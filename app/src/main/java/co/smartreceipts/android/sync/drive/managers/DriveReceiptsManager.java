package co.smartreceipts.android.sync.drive.managers;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactoryFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.TripsTable;
import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager;
import co.smartreceipts.automatic_backups.drive.rx.DriveStreamMappings;
import co.smartreceipts.automatic_backups.drive.rx.DriveStreamsManager;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.core.sync.provider.SyncProvider;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


public class DriveReceiptsManager {

    private final TableController<Receipt> mReceiptTableController;
    private final ReceiptsTable mReceiptsTable;
    private final TripsTable mTripsTable;
    private final DriveStreamsManager mDriveTaskManager;
    private final DriveDatabaseManager mDriveDatabaseManager;
    private final NetworkManager mNetworkManager;
    private final Analytics mAnalytics;
    private final DriveStreamMappings mDriveStreamMappings;
    private final ReceiptBuilderFactoryFactory mReceiptBuilderFactoryFactory;
    private final Scheduler mObserveOnScheduler;
    private final Scheduler mSubscribeOnScheduler;
    private final AtomicBoolean mIsEnabled = new AtomicBoolean(true);
    private final AtomicBoolean mIsInitializing = new AtomicBoolean(false);

    public DriveReceiptsManager(@NonNull TableController<Receipt> receiptsTableController, @NonNull TripsTable tripsTable,
                                @NonNull ReceiptsTable receiptsTable, @NonNull DriveStreamsManager driveTaskManager,
                                @NonNull DriveDatabaseManager driveDatabaseManager, @NonNull NetworkManager networkManager,
                                @NonNull Analytics analytics) {
        this(receiptsTableController, tripsTable, receiptsTable, driveTaskManager, driveDatabaseManager, networkManager, analytics, new DriveStreamMappings(), new ReceiptBuilderFactoryFactory(), Schedulers.io(), Schedulers.io());
    }

    public DriveReceiptsManager(@NonNull TableController<Receipt> receiptsTableController,
                                @NonNull TripsTable tripsTable,
                                @NonNull ReceiptsTable receiptsTable,
                                @NonNull DriveStreamsManager driveTaskManager,
                                @NonNull DriveDatabaseManager driveDatabaseManager,
                                @NonNull NetworkManager networkManager,
                                @NonNull Analytics analytics,
                                @NonNull DriveStreamMappings driveStreamMappings,
                                @NonNull ReceiptBuilderFactoryFactory receiptBuilderFactoryFactory,
                                @NonNull Scheduler observeOnScheduler,
                                @NonNull Scheduler subscribeOnScheduler) {
        mReceiptTableController = Preconditions.checkNotNull(receiptsTableController);
        mTripsTable = Preconditions.checkNotNull(tripsTable);
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mDriveTaskManager = Preconditions.checkNotNull(driveTaskManager);
        mDriveDatabaseManager = Preconditions.checkNotNull(driveDatabaseManager);
        mNetworkManager = Preconditions.checkNotNull(networkManager);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mDriveStreamMappings = Preconditions.checkNotNull(driveStreamMappings);
        mReceiptBuilderFactoryFactory = Preconditions.checkNotNull(receiptBuilderFactoryFactory);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @SuppressLint("CheckResult")
    public synchronized void syncReceipts() {
        if (mIsEnabled.get()) {
            if (mNetworkManager.isNetworkAvailable()) {
                if (!mIsInitializing.getAndSet(true)) {
                    Logger.info(this, "Performing initialization of drive receipts");
                    mTripsTable.get()
                            .flatMapObservable(Observable::fromIterable)
                            .flatMapSingle(trip -> mReceiptsTable.getUnsynced(trip, SyncProvider.GoogleDrive))
                            .flatMap(Observable::fromIterable)
                            .subscribeOn(mSubscribeOnScheduler)
                            .observeOn(mObserveOnScheduler)
                            .subscribe(receipt -> {
                                Logger.info(DriveReceiptsManager.this, "Performing found unsynced receipt " + receipt.getId());
                                if (receipt.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive)) {
                                    Logger.info(DriveReceiptsManager.this, "Handling delete action during initialization");
                                    handleDeleteInternal(receipt);
                                } else {
                                    Logger.info(DriveReceiptsManager.this, "Handling insert/update action during initialization");
                                    handleInsertOrUpdateInternal(receipt);
                                }
                            }, throwable -> {
                                mAnalytics.record(new ErrorEvent(DriveReceiptsManager.this, throwable));
                                Logger.error(DriveReceiptsManager.this, "Failed to fetch our unsynced receipt data", throwable);
                                mIsInitializing.set(false);
                            }, () -> {
                                mDriveDatabaseManager.syncDatabase();
                                mIsInitializing.set(false);
                            });
                }
            }
        }
    }

    public synchronized void enable() {
        Logger.info(this, "Enabling Drive Receipts Manager");
        mIsEnabled.set(true);
    }

    public synchronized void disable() {
        Logger.info(this, "Disabling Drive Receipts Manager");
        mIsEnabled.set(false);
    }

    public synchronized void handleInsertOrUpdate(@NonNull final Receipt receipt) {
        if (!mIsInitializing.get()) {
            handleInsertOrUpdateInternal(receipt);
        }
    }

    @VisibleForTesting
    synchronized void handleInsertOrUpdateInternal(@NonNull final Receipt receipt) {
        if (!mIsEnabled.get()) {
            Logger.warn(this, "Ignoring insert or update as we're currently disabled");
            return;
        }

        Preconditions.checkNotNull(receipt);
        Preconditions.checkArgument(!receipt.getSyncState().isSynced(SyncProvider.GoogleDrive), "Cannot sync an already synced receipt");
        Preconditions.checkArgument(!receipt.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive), "Cannot insert/update a receipt that is marked for deletion");

        if (mNetworkManager.isNetworkAvailable()) {
            onInsertOrUpdateObservable(receipt)
                    .observeOn(mObserveOnScheduler)
                    .subscribeOn(mSubscribeOnScheduler)
                    .map(syncState -> mReceiptBuilderFactoryFactory.build(receipt).setSyncState(syncState).build())
                    .flatMapObservable(newReceipt -> {
                            Logger.info(DriveReceiptsManager.this, "Updating receipt " + receipt.getId() + " to reflect its sync state");
                            return mReceiptTableController.update(receipt, newReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
                    })
                    .subscribe(newReceipt -> {
                            Logger.info(DriveReceiptsManager.this, "Successfully updated receipt " + receipt.getId() + " to reflect its sync state");
                    }, throwable -> {
                            mAnalytics.record(new ErrorEvent(DriveReceiptsManager.this, throwable));
                            Logger.error(DriveReceiptsManager.this, "Failed to handle insert/update for " + receipt.getId() + " to reflect its sync state", throwable);
                    });
        } else {
            Logger.warn(this, "No network. Skipping insert/update");
        }
    }

    public synchronized void handleDelete(@NonNull final Receipt receipt) {
        if (!mIsInitializing.get()) {
            handleDeleteInternal(receipt);
        }
    }

    @VisibleForTesting
    synchronized void handleDeleteInternal(@NonNull final Receipt receipt) {
        if (!mIsEnabled.get()) {
            Logger.warn(this, "Ignoring delete as we're currently disabled");
            return;
        }

        Preconditions.checkNotNull(receipt);
        Preconditions.checkArgument(!receipt.getSyncState().isSynced(SyncProvider.GoogleDrive), "Cannot delete an already synced receipt");
        Preconditions.checkArgument(receipt.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive), "Cannot delete a receipt that isn't marked for deletion");

        if (mNetworkManager.isNetworkAvailable()) {
            mReceiptsTable.findByPrimaryKey(receipt.getId())
                    .flatMap(queriedReceipt -> {
                        if (receipt.equals(queriedReceipt) && receipt.getIndex() == queriedReceipt.getIndex()) {
                            return mDriveTaskManager.deleteDriveFile(receipt.getSyncState(), true);
                        } else {
                            Logger.warn(this, "Receipt {} appears to no longer match the requested one for deletion. Ignoring...", receipt.getId());
                            return Single.error(new Exception("Queued receipt for syncing is stale. Ignoring"));
                        }
                    })
                    .flatMapObservable(syncState -> Observable.just(mReceiptBuilderFactoryFactory.build(receipt).setSyncState(syncState).build()))
                    .observeOn(mObserveOnScheduler)
                    .subscribeOn(mSubscribeOnScheduler)
                    .subscribe(newReceipt -> {
                        Logger.info(DriveReceiptsManager.this, "Attempting to fully delete receipt {} that is marked for deletion", receipt.getId());
                        mReceiptTableController.delete(newReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
                    }, throwable -> {
                        mAnalytics.record(new ErrorEvent(DriveReceiptsManager.this, throwable));
                        Logger.error(DriveReceiptsManager.this, "Failed to handle delete for " + receipt.getId() + " to reflect its sync state", throwable);
                    });
        } else {
            Logger.warn(DriveReceiptsManager.this, "No network. Skipping delete");
        }
    }

    @NonNull
    private Single<SyncState> onInsertOrUpdateObservable(@NonNull final Receipt receipt) {
        final SyncState oldSyncState = receipt.getSyncState();
        final File receiptFile = receipt.getFile();

        return mReceiptsTable.findByPrimaryKey(receipt.getId())
                .flatMap(queriedReceipt -> {
                    if (receipt.equals(queriedReceipt)) {
                        if (oldSyncState.getSyncId(SyncProvider.GoogleDrive) == null) {
                            if (receiptFile != null && receiptFile.exists()) {
                                Logger.info(this, "Found receipt {} with a non-uploaded file. Uploading", receipt.getId());
                                return mDriveTaskManager.uploadFileToDrive(oldSyncState, receiptFile);
                            } else {
                                Logger.info(this, "Found receipt {} without a file. Marking as synced for Drive", receipt.getId());
                                return Single.just(mDriveStreamMappings.postInsertSyncState());
                            }
                        } else {
                            if (receiptFile != null) {
                                Logger.info(this, "Found receipt {} with a new file. Updating", receipt.getId());
                                return mDriveTaskManager.updateDriveFile(oldSyncState, receiptFile);
                            } else {
                                Logger.info(this, "Found receipt {} with a stale file reference. Removing", receipt.getId());
                                return mDriveTaskManager.deleteDriveFile(oldSyncState, false);
                            }
                        }
                    } else {
                        Logger.warn(this, "Receipt {} appears to no longer match the requested one for upload. Ignoring...", receipt.getId());
                        return Single.error(new Exception("Queued receipt for syncing is stale. Ignoring"));
                    }
                });
    }

}
