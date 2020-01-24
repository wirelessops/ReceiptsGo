package co.smartreceipts.android.sync.drive;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.restore.DatabaseRestorer;
import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.managers.DriveDatabaseManager;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;
import co.smartreceipts.android.sync.drive.managers.DriveRestoreDataManager;
import co.smartreceipts.android.sync.drive.managers.GoogleDriveTableManager;
import co.smartreceipts.android.sync.drive.rx.DriveClientInitializer;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.errors.CriticalSyncError;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.core.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.sync.noop.NoOpBackupProvider;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class GoogleDriveBackupManager implements BackupProvider {

    private static final int REQUEST_CODE_GOOGLE_SERVICE_AUTH = 712;
    private static final int REQUEST_CODE_GOOGLE_SERVICE_REAUTH = 713;

    private Activity activity;
    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final GoogleDriveTableManager googleDriveTableManager;
    private final NetworkManager networkManager;
    private final Analytics analytics;
    private final ReceiptTableController receiptTableController;
    private final DatabaseRestorer databaseRestorer;
    private final NoOpBackupProvider noOpBackupProvider;
    private final GoogleDriveSyncMetadata googleDriveSyncMetadata;

    private final BehaviorSubject<Optional<Throwable>> syncErrorStream = BehaviorSubject.create();
    private final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private final Object initializationLock = new Object();

    // Drive objects that will be created during the initialization flow
    private DriveClientInitializer driveClientInitializer;

    // An activity reference
    private WeakReference<FragmentActivity> activityReference = new WeakReference<>(null);

    @Inject
    public GoogleDriveBackupManager(@NonNull Context context,
                                    @NonNull DatabaseHelper databaseHelper,
                                    @NonNull GoogleDriveTableManager googleDriveTableManager,
                                    @NonNull NetworkManager networkManager,
                                    @NonNull Analytics analytics,
                                    @NonNull ReceiptTableController receiptTableController,
                                    @NonNull DatabaseRestorer databaseRestorer,
                                    @NonNull NoOpBackupProvider noOpBackupProvider) {

        this.context = context;
        this.databaseHelper = databaseHelper;
        this.networkManager = networkManager;
        this.analytics = analytics;
        this.receiptTableController = receiptTableController;
        this.databaseRestorer = databaseRestorer;
        this.googleDriveTableManager = googleDriveTableManager;
        this.noOpBackupProvider = noOpBackupProvider;
        this.googleDriveSyncMetadata = new GoogleDriveSyncMetadata(context);
    }

    @Override
    public void initialize(@NonNull FragmentActivity activity) {
        this.activity = activity;

        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(activity, "Google Drive requires a valid activity to be provided");

        final boolean canInitialize;
        synchronized (initializationLock) {
            canInitialize = driveClientInitializer == null && !isInitializing.getAndSet(true);
        }

        if (canInitialize) {
            Logger.info(this, "Initializing Drive Backup Provider");

            final FragmentActivity existingActivity = activityReference.get();
            if (!activity.equals(existingActivity)) {
                activityReference = new WeakReference<>(activity);
            }

            final GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(activity);
            if (signInAccount == null) {
                final GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE), new Scope(DriveScopes.DRIVE_APPDATA))
                        .build();
                final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
                activity.startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_GOOGLE_SERVICE_AUTH);
            } else {
                onGoogleSignInAccountReady(signInAccount);
            }
        } else {
            Logger.debug(this, "Our Google Drive manager is already initialized or initializing");
        }
    }

    @Override
    public void deinitialize() {
        synchronized (initializationLock) {
            Logger.info(this, "De-initializing Drive Backup Provider");
            isInitializing.set(false);
            if (driveClientInitializer != null) {
                driveClientInitializer.deinitialize();
                driveClientInitializer = null;
            }
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Logger.debug(this, "Handling drive request. request = {}, result = {}", requestCode, resultCode);
        if ((requestCode == REQUEST_CODE_GOOGLE_SERVICE_AUTH || requestCode == REQUEST_CODE_GOOGLE_SERVICE_REAUTH)
                && resultCode == Activity.RESULT_OK) {
            final Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (signInAccountTask.isSuccessful()) {
                Logger.info(this, "Successfully authorized our Google Drive account");
                onGoogleSignInAccountReady(signInAccountTask.getResult());
            } else {
                Logger.error(this, "Failed to successfully authorize our Google Drive account");
            }
            return true;
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public Single<List<RemoteBackupMetadata>> getRemoteBackups() {
        final DriveClientInitializer driveClientInitializerRef;
        synchronized (initializationLock) {
            driveClientInitializerRef = driveClientInitializer;
        }
        if (driveClientInitializerRef != null) {
            return driveClientInitializerRef.getDriveStreamsManager().getRemoteBackups();
        } else {
            return noOpBackupProvider.getRemoteBackups();
        }
    }

    @Nullable
    @Override
    public Identifier getDeviceSyncId() {
        return googleDriveSyncMetadata.getDeviceIdentifier();
    }

    @NonNull
    @Override
    public Date getLastDatabaseSyncTime() {
        return googleDriveSyncMetadata.getLastDatabaseSyncTime();
    }

    @NonNull
    @Override
    public Single<Boolean> restoreBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean overwriteExistingData) {
        Logger.info(this, "Restoring drive backup: {}", remoteBackupMetadata);
        final DriveClientInitializer driveClientInitializerRef;
        synchronized (initializationLock) {
            driveClientInitializerRef = driveClientInitializer;
        }
        if (driveClientInitializerRef != null) {
            return driveClientInitializerRef.getDriveRestoreDataManager().restoreBackup(remoteBackupMetadata, overwriteExistingData);
        } else {
            return noOpBackupProvider.restoreBackup(remoteBackupMetadata, overwriteExistingData);
        }
    }

    @NonNull
    @Override
    public Single<Boolean> deleteBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(remoteBackupMetadata);
        Logger.info(this, "Deleting drive backup: {}", remoteBackupMetadata);

        final DriveClientInitializer driveClientInitializerRef;
        synchronized (initializationLock) {
            driveClientInitializerRef = driveClientInitializer;
        }
        if (driveClientInitializerRef != null) {
            if (remoteBackupMetadata.getSyncDeviceId().equals(googleDriveSyncMetadata.getDeviceIdentifier())) {
                Logger.info(this, "The backup is our local backup");
                googleDriveSyncMetadata.clear();
                driveClientInitializerRef.getDriveReceiptsManager().disable();
            }

            return driveClientInitializerRef.getDriveStreamsManager().delete(remoteBackupMetadata.getId())
                    .doOnSuccess(success -> {
                        driveClientInitializerRef.getDriveReceiptsManager().enable();
                        if (success) {
                            driveClientInitializerRef.getDriveReceiptsManager().syncReceipts();
                        }
                    });
        } else {
            return noOpBackupProvider.deleteBackup(remoteBackupMetadata);
        }
    }

    @Override
    public Single<Boolean> clearCurrentBackupConfiguration() {
        Logger.info(this, "Clearing our current backup configuration");

        final DriveClientInitializer driveClientInitializerRef;
        synchronized (initializationLock) {
            driveClientInitializerRef = driveClientInitializer;
        }
        if (driveClientInitializerRef != null) {
            driveClientInitializerRef.getDriveReceiptsManager().disable();
            googleDriveSyncMetadata.clear();
            driveClientInitializerRef.getDriveStreamsManager().clearCachedData();
            // Note: We added a stupid delay hack here to allow things to clear out of their buffers
            return Single.just(true)
                    .delay(500, TimeUnit.MILLISECONDS)
                    .doOnSuccess(success -> {
                        driveClientInitializerRef.getDriveReceiptsManager().enable();
                        if (success) {
                            driveClientInitializerRef.getDriveReceiptsManager().syncReceipts();
                        }
                    });
        } else {
            return noOpBackupProvider.clearCurrentBackupConfiguration();
        }
    }

    @NonNull
    @Override
    public Single<List<File>> downloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        final DriveClientInitializer driveClientInitializerRef;
        synchronized (initializationLock) {
            driveClientInitializerRef = driveClientInitializer;
        }
        if (driveClientInitializerRef != null) {
            return driveClientInitializerRef.getDriveRestoreDataManager().downloadAllBackupMetadataImages(remoteBackupMetadata, downloadLocation);
        } else {
            return noOpBackupProvider.downloadAllData(remoteBackupMetadata, downloadLocation);
        }
    }

    @NonNull
    @Override
    public Single<List<File>> debugDownloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        final DriveClientInitializer driveClientInitializerRef;
        synchronized (initializationLock) {
            driveClientInitializerRef = driveClientInitializer;
        }
        if (driveClientInitializerRef != null) {
            return driveClientInitializerRef.getDriveRestoreDataManager().downloadAllFilesInDriveFolder(remoteBackupMetadata, downloadLocation);
        } else {
            return noOpBackupProvider.debugDownloadAllData(remoteBackupMetadata, downloadLocation);
        }
    }

    @NonNull
    @Override
    public Observable<CriticalSyncError> getCriticalSyncErrorStream() {
        return syncErrorStream.filter(Optional::isPresent)
                .map(Optional::get)
                .<Optional<CriticalSyncError>>map(throwable -> {
                    if (throwable instanceof CriticalSyncError) {
                        return Optional.of((CriticalSyncError) throwable);
                    } else {
                        return Optional.absent();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    @Override
    public void markErrorResolved(@NonNull SyncErrorType syncErrorType) {
        syncErrorStream.onNext(Optional.absent());
    }

    @SuppressLint("CheckResult")
    private void onGoogleSignInAccountReady(@NonNull GoogleSignInAccount signInAccount) {
        // First, confirm that we're still initializing
        synchronized (initializationLock) {
            if (isInitializing.get()) {
                Single.fromCallable(() -> {
                    final String scopes = "oauth2:" + DriveScopes.DRIVE_APPDATA + " " + DriveScopes.DRIVE_FILE;
                    return GoogleAuthUtil.getToken(context, signInAccount.getAccount(), scopes);
                }).doOnError(throwable ->  {
                    Logger.error(GoogleDriveBackupManager.this, "Failed to authenticate user with status: {}", throwable.getMessage());
                }).subscribeOn(Schedulers.io())
                        .subscribe(token -> GoogleSignInAccountFinalization(signInAccount, token),
                                onError -> {
                            if (onError instanceof UserRecoverableAuthException) {
                                activity.startActivityForResult(((UserRecoverableAuthException) onError).getIntent(), REQUEST_CODE_GOOGLE_SERVICE_REAUTH);
                            } else if (onError instanceof UserRecoverableAuthIOException) {
                                activity.startActivityForResult(((UserRecoverableAuthIOException) onError).getIntent(), REQUEST_CODE_GOOGLE_SERVICE_REAUTH);
                            }
                        });
            }
        }
    }

    private void GoogleSignInAccountFinalization(@NonNull GoogleSignInAccount signInAccount, String token) {
        Collection<String> scopes = new ArrayList<>();
        scopes.add(DriveScopes.DRIVE_FILE);
        scopes.add(DriveScopes.DRIVE_APPDATA);
        // Use the authenticated account to sign in to the Drive service.
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(context, scopes);
        credential.setSelectedAccount(signInAccount.getAccount());

        Drive googleDriveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                setHttpTimeout(credential))
                .setApplicationName("Smart Receipts")
                .build();

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        DriveServiceHelper driveServiceHelper = new DriveServiceHelper(context, googleDriveService);

        // Next, build each of the appropriate member objects
        final DriveStreamsManager driveStreamsManager = new DriveStreamsManager(context, driveServiceHelper, googleDriveSyncMetadata, syncErrorStream);
        final DriveDatabaseManager driveDatabaseManager = new DriveDatabaseManager(context, driveStreamsManager, googleDriveSyncMetadata, analytics);
        final DriveReceiptsManager driveReceiptsManager = new DriveReceiptsManager(receiptTableController, databaseHelper.getTripsTable(), databaseHelper.getReceiptsTable(),
                driveStreamsManager, driveDatabaseManager, this.networkManager, analytics);
        final DriveRestoreDataManager driveRestoreDataManager = new DriveRestoreDataManager(context, driveStreamsManager, driveDatabaseManager, databaseRestorer);

        this.driveClientInitializer = new DriveClientInitializer(driveStreamsManager, driveReceiptsManager, driveDatabaseManager,
                driveRestoreDataManager, googleDriveTableManager, networkManager);

        driveClientInitializer.initialize();

        isInitializing.set(false);
    }

    private HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return httpRequest -> {
            requestInitializer.initialize(httpRequest);
            httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
            httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
        };
    }

}
