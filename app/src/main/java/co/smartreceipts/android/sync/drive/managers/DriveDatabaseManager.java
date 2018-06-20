package co.smartreceipts.android.sync.drive.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


public class DriveDatabaseManager {

    private final Context mContext;
    private final DriveStreamsManager mDriveTaskManager;
    private final GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;
    private final Analytics mAnalytics;
    private final Scheduler mObserveOnScheduler;
    private final Scheduler mSubscribeOnScheduler;
    private final AtomicBoolean mIsSyncInProgress = new AtomicBoolean(false);

    public DriveDatabaseManager(@NonNull Context context, @NonNull DriveStreamsManager driveTaskManager,
                                @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                                @NonNull Analytics analytics) {
        this(context, driveTaskManager, googleDriveSyncMetadata, analytics, Schedulers.io(), Schedulers.io());
    }

    public DriveDatabaseManager(@NonNull Context context, @NonNull DriveStreamsManager driveTaskManager, @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                                @NonNull Analytics analytics, @NonNull Scheduler observeOnScheduler, @NonNull Scheduler subscribeOnScheduler) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mDriveTaskManager = Preconditions.checkNotNull(driveTaskManager);
        mGoogleDriveSyncMetadata = Preconditions.checkNotNull(googleDriveSyncMetadata);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @SuppressLint("CheckResult")
    public void syncDatabase() {
        // TODO: Make sure the database is closed or inactive before performing this
        // TODO: We can trigger this off of our #close() method in DB helper
        final File filesDir = mContext.getExternalFilesDir(null);
        if (filesDir != null) {
            final File dbFile = new File(filesDir, DatabaseHelper.DATABASE_NAME);
            if (dbFile.exists()) {
                if (!mIsSyncInProgress.getAndSet(true)) {
                    getSyncDatabaseObservable(dbFile)
                            .observeOn(mObserveOnScheduler)
                            .subscribeOn(mSubscribeOnScheduler)
                            .subscribe(identifier -> {
                                Logger.info(DriveDatabaseManager.this, "Successfully synced our database");
                                mGoogleDriveSyncMetadata.setDatabaseSyncIdentifier(identifier);
                                mIsSyncInProgress.set(false);
                            }, throwable -> {
                                mIsSyncInProgress.set(false);
                                mAnalytics.record(new ErrorEvent(DriveDatabaseManager.this, throwable));
                                Logger.error(DriveDatabaseManager.this, "Failed to synced our database", throwable);
                            });
                } else {
                    Logger.debug(DriveDatabaseManager.this, "A sync is already in progress. Ignoring subsequent one for now");
                }
            } else {
                Logger.error(DriveDatabaseManager.this, "Failed to find our main database");
            }
        } else {
            Logger.error(DriveDatabaseManager.this, "Failed to find our main database storage directory");
        }
    }

    @NonNull
    private Single<Identifier> getSyncDatabaseObservable(@NonNull final File dbFile) {
        final Identifier driveDatabaseId = mGoogleDriveSyncMetadata.getDatabaseSyncIdentifier();
        if (driveDatabaseId != null) {
            return mDriveTaskManager.updateDriveFile(driveDatabaseId, dbFile);
        } else {
            return mDriveTaskManager.uploadFileToDrive(dbFile);
        }
    }
}
