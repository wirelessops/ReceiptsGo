package co.smartreceipts.android.sync.drive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.restore.DatabaseRestorer;
import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.managers.DriveDatabaseManager;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;
import co.smartreceipts.android.sync.drive.managers.DriveRestoreDataManager;
import co.smartreceipts.android.sync.drive.managers.GoogleDriveTableManager;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.drive.services.DriveUploadCompleteManager;
import co.smartreceipts.android.sync.errors.CriticalSyncError;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.sync.network.NetworkStateChangeListener;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;


public class GoogleDriveBackupManager implements BackupProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, NetworkStateChangeListener {

    /**
     * Request code for auto Google Play Services error resolution.
     */
    private static final int REQUEST_CODE_RESOLUTION = 1;

    private final GoogleDriveTableManager mGoogleDriveTableManager;
    private final GoogleApiClient mGoogleApiClient;
    private final DriveStreamsManager mDriveTaskManager;
    private final AtomicReference<WeakReference<FragmentActivity>> mActivityReference;
    private final NetworkManager mNetworkManager;
    private final GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;
    private final DriveReceiptsManager mDriveReceiptsManager;
    private final DriveRestoreDataManager mDriveRestoreDataManager;
    private final BehaviorSubject<Optional<Throwable>> mSyncErrorStream;

    @Inject
    public GoogleDriveBackupManager(@NonNull Context context,
                                    @NonNull DatabaseHelper databaseHelper,
                                    @NonNull GoogleDriveTableManager googleDriveTableManager,
                                    @NonNull NetworkManager networkManager,
                                    @NonNull Analytics analytics,
                                    @NonNull ReceiptTableController receiptTableController,
                                    @NonNull DriveUploadCompleteManager driveUploadCompleteManager,
                                    @NonNull DatabaseRestorer databaseRestorer) {

        mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .useDefaultAccount()
                .build();

        mGoogleDriveSyncMetadata = new GoogleDriveSyncMetadata(context);
        mNetworkManager = networkManager;
        mSyncErrorStream = BehaviorSubject.create();
        mDriveTaskManager = new DriveStreamsManager(context, mGoogleApiClient, mGoogleDriveSyncMetadata, mSyncErrorStream, driveUploadCompleteManager);
        mActivityReference = new AtomicReference<>(new WeakReference<FragmentActivity>(null));

        final DriveDatabaseManager driveDatabaseManager = new DriveDatabaseManager(context,
                mDriveTaskManager, mGoogleDriveSyncMetadata, mNetworkManager, analytics);
        mDriveReceiptsManager = new DriveReceiptsManager(receiptTableController, databaseHelper.getTripsTable(), databaseHelper.getReceiptsTable(),
                mDriveTaskManager, driveDatabaseManager, mNetworkManager, analytics);
        mDriveRestoreDataManager = new DriveRestoreDataManager(context, mDriveTaskManager, databaseHelper, driveDatabaseManager, databaseRestorer);

        mGoogleDriveTableManager = googleDriveTableManager;
        mGoogleDriveTableManager.initBackupListeners(driveDatabaseManager, mDriveReceiptsManager);
    }

    @Override
    public void initialize(@Nullable FragmentActivity activity) {
        Preconditions.checkNotNull(activity, "Google Drive requires a valid activity to be provided");
        Logger.info(this, "Initializing Drive Backup Provider");

        final FragmentActivity existingActivity = mActivityReference.get().get();
        if (!activity.equals(existingActivity)) {
            mActivityReference.set(new WeakReference<>(activity));
        }
        if (!isConnectedOrConnecting()) {
            mGoogleApiClient.connect();
        }
        mNetworkManager.registerListener(this);
    }

    @Override
    public void deinitialize() {
        Logger.info(this, "De-initializing Drive Backup Provider");
        mNetworkManager.unregisterListener(this);
        if (isConnectedOrConnecting()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Logger.debug(this, "Handling drive request. request = {}, result = {}", requestCode, resultCode);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            if (!isConnectedOrConnecting()) {
                Logger.debug(this, "Initiating Drive Connection now that permissions have been granted");
                mGoogleApiClient.connect();
            }
            return true;
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public Single<List<RemoteBackupMetadata>> getRemoteBackups() {
        return mDriveTaskManager.getRemoteBackups();
    }

    @Nullable
    @Override
    public Identifier getDeviceSyncId() {
        return mGoogleDriveSyncMetadata.getDeviceIdentifier();
    }

    @NonNull
    @Override
    public Date getLastDatabaseSyncTime() {
        return mGoogleDriveSyncMetadata.getLastDatabaseSyncTime();
    }

    @NonNull
    @Override
    public Single<Boolean> restoreBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean overwriteExistingData) {
        Logger.info(this, "Restoring drive backup: {}", remoteBackupMetadata);
        return mDriveRestoreDataManager.restoreBackup(remoteBackupMetadata, overwriteExistingData);
    }

    @NonNull
    @Override
    public Single<Boolean> deleteBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Logger.info(this, "Deleting drive backup: {}", remoteBackupMetadata);

        if (remoteBackupMetadata.getSyncDeviceId().equals(mGoogleDriveSyncMetadata.getDeviceIdentifier())) {
            Logger.info(this, "The backup is our local backup");
            mGoogleDriveSyncMetadata.clear();
            mDriveReceiptsManager.disable();
        }

        return mDriveTaskManager.delete(remoteBackupMetadata.getId())
                .doOnSuccess(success -> {
                    mDriveReceiptsManager.enable();
                    if (success) {
                        mDriveReceiptsManager.initialize();
                    }
                });
    }

    @Override
    public Single<Boolean> clearCurrentBackupConfiguration() {
        Logger.info(this, "Clearing our current backup configuration");
        mDriveReceiptsManager.disable();
        mGoogleDriveSyncMetadata.clear();
        mDriveTaskManager.clearCachedData();
        // Note: We added a stupid delay hack here to allow things to clear out of their buffers
        return Single.just(true)
                .delay(500, TimeUnit.MILLISECONDS)
                .doOnSuccess(success -> {
                        mDriveReceiptsManager.enable();
                        if (success) {
                            mDriveReceiptsManager.initialize();
                        }
                });
    }

    @NonNull
    @Override
    public Single<List<File>> downloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        return mDriveRestoreDataManager.downloadAllBackupMetadataImages(remoteBackupMetadata, downloadLocation);
    }

    @NonNull
    @Override
    public Single<List<File>> debugDownloadAllData(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull File downloadLocation) {
        return mDriveRestoreDataManager.downloadAllFilesInDriveFolder(remoteBackupMetadata, downloadLocation);
    }

    @NonNull
    @Override
    public Observable<CriticalSyncError> getCriticalSyncErrorStream() {
        return mSyncErrorStream.filter(Optional::isPresent)
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
        mSyncErrorStream.onNext(Optional.absent());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Logger.warn(this, "GoogleApiClient connection failed: {}", result);

        final FragmentActivity activity = mActivityReference.get().get();
        if (activity == null) {
            Logger.error(this, "The parent activity was destroyed. Unable to resolve GoogleApiClient connection failure.");
            return;
        }

        try {
            if (!result.hasResolution()) {
                GoogleApiAvailability.getInstance().getErrorDialog(activity, result.getErrorCode(), 0).show();
                return;
            }
            try {
                result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Logger.error(this, "Exception while starting resolution activity", e);
            }
        } catch (IllegalStateException e) {
            Logger.warn(this,  "The parent activity is in a bad state.. Unable to resolve GoogleApiClient connection failure.");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mDriveTaskManager.onConnected(bundle);
        mDriveReceiptsManager.initialize();

        mGoogleDriveTableManager.onConnected();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleDriveTableManager.onConnectionSuspended();

        mDriveTaskManager.onConnectionSuspended(cause);
    }

    @Override
    public void onNetworkConnectivityLost() {

    }

    @Override
    public void onNetworkConnectivityGained() {
        Logger.info(this, "Handling a NetworkConnectivityGained event for drive");
        if (!isConnectedOrConnecting()) {
            final FragmentActivity existingActivity = mActivityReference.get().get();
            if (existingActivity != null) {
                initialize(existingActivity);
            }
        } else {
            mDriveReceiptsManager.initialize();
        }
    }

    private boolean isConnectedOrConnecting() {
        return mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting();
    }
}
