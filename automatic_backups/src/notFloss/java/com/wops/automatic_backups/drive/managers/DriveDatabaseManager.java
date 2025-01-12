package com.wops.automatic_backups.drive.managers;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.ErrorEvent;
import com.wops.analytics.log.Logger;
import com.wops.automatic_backups.drive.device.GoogleDriveSyncMetadata;
import com.wops.automatic_backups.drive.rx.DriveStreamsManager;
import com.wops.core.persistence.DatabaseConstants;
import com.wops.core.sync.model.impl.Identifier;
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
            final File dbFile = new File(filesDir, DatabaseConstants.DATABASE_NAME);
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
                                Logger.error(DriveDatabaseManager.this, "Failed to sync our database", throwable);
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
