package co.smartreceipts.android.sync.drive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.restore.DatabaseRestorer;
import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;
import co.smartreceipts.android.sync.drive.managers.DriveRestoreDataManager;
import co.smartreceipts.android.sync.drive.managers.GoogleDriveTableManager;
import co.smartreceipts.android.sync.drive.rx.DriveClientInitializer;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.sync.noop.NoOpBackupProvider;
import co.smartreceipts.automatic_backups.drive.DriveAccountHelper;
import co.smartreceipts.automatic_backups.drive.DriveServiceHelper;
import co.smartreceipts.automatic_backups.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager;
import co.smartreceipts.automatic_backups.drive.managers.DriveDownloader;
import co.smartreceipts.automatic_backups.drive.rx.DriveStreamsManager;
import co.smartreceipts.core.sync.errors.CriticalSyncError;
import co.smartreceipts.core.sync.errors.SyncErrorType;
import co.smartreceipts.core.sync.model.RemoteBackupMetadata;
import co.smartreceipts.core.sync.model.impl.Identifier;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

public class GoogleDriveBackupManager implements BackupProvider {

    private static final int REQUEST_CODE_GOOGLE_SERVICE_AUTH = 712;
    private static final int REQUEST_CODE_GOOGLE_SERVICE_REAUTH = 713;

    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final GoogleDriveTableManager googleDriveTableManager;
    private final NetworkManager networkManager;
    private final Analytics analytics;
    private final ReceiptTableController receiptTableController;
    private final DatabaseRestorer databaseRestorer;
    private final NoOpBackupProvider noOpBackupProvider;
    private final GoogleDriveSyncMetadata googleDriveSyncMetadata;

    private final DriveAccountHelper driveAccountHelper;

    private final BehaviorSubject<Optional<Throwable>> syncErrorStream = BehaviorSubject.create();
    private final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private final Object initializationLock = new Object();

    // Drive objects that will be created during the initialization flow
    private DriveClientInitializer driveClientInitializer;

    // An activity reference
    private WeakReference<FragmentActivity> activityReference = new WeakReference<>(null);

    private CompositeDisposable intentsCompositeDisposable = new CompositeDisposable();

    @Inject
    GoogleDriveBackupManager(@NonNull Context context,
                             @NonNull DatabaseHelper databaseHelper,
                             @NonNull GoogleDriveTableManager googleDriveTableManager,
                             @NonNull NetworkManager networkManager,
                             @NonNull Analytics analytics,
                             @NonNull ReceiptTableController receiptTableController,
                             @NonNull DatabaseRestorer databaseRestorer,
                             @NonNull NoOpBackupProvider noOpBackupProvider,
                             @NonNull DriveAccountHelper driveAccountHelper) {

        this.context = context;
        this.databaseHelper = databaseHelper;
        this.networkManager = networkManager;
        this.analytics = analytics;
        this.receiptTableController = receiptTableController;
        this.databaseRestorer = databaseRestorer;
        this.googleDriveTableManager = googleDriveTableManager;
        this.noOpBackupProvider = noOpBackupProvider;
        this.googleDriveSyncMetadata = new GoogleDriveSyncMetadata(context);
        this.driveAccountHelper = driveAccountHelper;
    }

    @Override
    public void initialize(@NonNull FragmentActivity activity) {
        intentsCompositeDisposable.add(driveAccountHelper.getSignInIntentsSubject()
                .subscribe(intent -> activity.startActivityForResult(intent, REQUEST_CODE_GOOGLE_SERVICE_AUTH)));
        intentsCompositeDisposable.add(driveAccountHelper.getErrorIntentsSubject()
                .subscribe(intent -> activity.startActivityForResult(intent, REQUEST_CODE_GOOGLE_SERVICE_REAUTH)));


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

            driveAccountHelper.signIn(activity)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .subscribe(this::googleSignInAccountFinalization, throwable -> Logger.error(this, throwable.getMessage()));

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
            intentsCompositeDisposable.clear();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Logger.debug(this, "Handling drive request. request = {}, result = {}", requestCode, resultCode);
        if ((requestCode == REQUEST_CODE_GOOGLE_SERVICE_AUTH || requestCode == REQUEST_CODE_GOOGLE_SERVICE_REAUTH)
                && resultCode == Activity.RESULT_OK) {

            synchronized (initializationLock) {
                if (isInitializing.get()) {
                    driveAccountHelper.processResultIntent(data, context)
                            .subscribe(driveServiceHelper -> googleSignInAccountFinalization(driveServiceHelper),
                                    throwable -> Logger.error(this, throwable.getMessage()));
                }
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

    private void googleSignInAccountFinalization(DriveServiceHelper driveServiceHelper) {

        // Next, build each of the appropriate member objects
        final DriveStreamsManager driveStreamsManager = new DriveStreamsManager(context, driveServiceHelper, googleDriveSyncMetadata, syncErrorStream);
        final DriveDatabaseManager driveDatabaseManager = new DriveDatabaseManager(context, driveStreamsManager, googleDriveSyncMetadata, analytics);
        final DriveReceiptsManager driveReceiptsManager = new DriveReceiptsManager(receiptTableController, databaseHelper.getTripsTable(), databaseHelper.getReceiptsTable(),
                driveStreamsManager, driveDatabaseManager, this.networkManager, analytics);
        final DriveDownloader driveDownloader = new DriveDownloader(driveStreamsManager);
        final DriveRestoreDataManager driveRestoreDataManager = new DriveRestoreDataManager(context, driveDatabaseManager, databaseRestorer, driveDownloader);

        this.driveClientInitializer = new DriveClientInitializer(driveStreamsManager, driveReceiptsManager, driveDatabaseManager,
                driveRestoreDataManager, googleDriveTableManager, networkManager);

        driveClientInitializer.initialize();

        isInitializing.set(false);
    }

}
