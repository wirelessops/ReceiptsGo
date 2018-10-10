package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

public class TripTableActionAlterations extends StubTableActionAlterations<Trip> {

    private final Table<Trip> mTripsTable;
    private final ReceiptsTable mReceiptsTable;
    private final DistanceTable mDistanceTable;
    private final DatabaseHelper mDatabaseHelper;
    private final StorageManager mStorageManager;

    public TripTableActionAlterations(@NonNull PersistenceManager persistenceManager) {
        this(Preconditions.checkNotNull(persistenceManager).getDatabase(), Preconditions.checkNotNull(persistenceManager).getStorageManager());
    }

    public TripTableActionAlterations(@NonNull DatabaseHelper databaseHelper, @NonNull StorageManager storageManager) {
        this(Preconditions.checkNotNull(databaseHelper).getTripsTable(), databaseHelper.getReceiptsTable(), databaseHelper.getDistanceTable(), databaseHelper, storageManager);
    }

    public TripTableActionAlterations(@NonNull Table<Trip> tripsTable, @NonNull ReceiptsTable receiptsTable, @NonNull DistanceTable distanceTable,
                                      @NonNull DatabaseHelper databaseHelper, @NonNull StorageManager storageManager) {
        mTripsTable = Preconditions.checkNotNull(tripsTable);
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mDistanceTable = Preconditions.checkNotNull(distanceTable);
        mDatabaseHelper = Preconditions.checkNotNull(databaseHelper);
        mStorageManager = Preconditions.checkNotNull(storageManager);
    }

    @NonNull
    @Override
    public Single<List<Trip>> postGet(@NonNull final List<Trip> trips) {
        return Observable.just(trips)
                .flatMapIterable(trips1 -> trips1)
                .doOnNext(mDatabaseHelper::getTripPriceAndDailyPrice)
                .toList();
    }

    @NonNull
    @Override
    public Single<Trip> postInsert(@NonNull final Trip postInsertTrip)  {
            if (postInsertTrip == null) {
                return Single.error(new Exception("Post insert failed due to a null trip"));
            }

            return makeTripDirectory(postInsertTrip)
                    .doOnError(throwable -> {
                        mTripsTable.delete(postInsertTrip, new DatabaseOperationMetadata(OperationFamilyType.Rollback))
                                .subscribe();
                    })
                    .andThen(Single.just(postInsertTrip))
                    .doOnSuccess(trip -> backUpDatabase());
    }


    @NonNull
    private Completable makeTripDirectory(@NonNull final Trip trip) {
        return Completable.fromAction(() -> {
            File directory = mStorageManager.mkdir(trip.getName());

            if (directory == null) {
                throw new IOException("Make trip directory failed");
            }
        });
    }

    @NonNull
    @Override
    public Single<Trip> postUpdate(@NonNull final Trip oldTrip, @Nullable final Trip newTrip) {
        return Single.fromCallable(() -> {
            if (newTrip == null) {
                throw new Exception("Post update failed due to a null trip");
            }

            newTrip.setPrice(oldTrip.getPrice());
            newTrip.setDailySubTotal(oldTrip.getDailySubTotal());

            return newTrip;
        }).doOnSuccess(trip -> {
            if (!oldTrip.getDirectory().equals(trip.getDirectory())) {

                mReceiptsTable.updateParentBlocking(oldTrip, trip);
                mDistanceTable.updateParentBlocking(oldTrip, trip);
                final File dir = mStorageManager.rename(oldTrip.getDirectory(), trip.getName());
                if (dir.equals(oldTrip.getDirectory())) {
                    Logger.error(this, "Failed to re-name the trip directory... Rolling back and throwing an exception");
                    mTripsTable.update(trip, oldTrip, new DatabaseOperationMetadata()).blockingGet();
                    mReceiptsTable.updateParentBlocking(trip, oldTrip);
                    mDistanceTable.updateParentBlocking(trip, oldTrip);

                    throw new IOException("Failed to create trip directory");
                }
            }
        });
    }

    @NonNull
    @Override
    public Single<Trip> postDelete(@Nullable final Trip trip) {
        return Single.fromCallable(() -> {
            if (trip == null) {
                throw new Exception("Post delete failed due to a null trip");
            }

            mReceiptsTable.deleteParentBlocking(trip);
            mDistanceTable.deleteParentBlocking(trip);
            if (!mStorageManager.deleteRecursively(trip.getDirectory())) {
                // TODO: Create clean up script
                Logger.error(this, "Failed to fully delete the underlying data. Create a clean up script to fix this later");
            }
            return trip;
        });
    }

    /**
     * Simple utility method that takes a snapshot backup of our database after all trip "insert'
     */
    private void backUpDatabase() {
        File sdDB = mStorageManager.getFile(DateUtils.getCurrentDateAsYYYY_MM_DDString() + "_" + DatabaseHelper.DATABASE_NAME + ".bak");
        try {
            if (mStorageManager.copy(mStorageManager.getFile(DatabaseHelper.DATABASE_NAME), sdDB, true)) {
                Logger.info(this, "Backed up database file to: {}", sdDB.getName());
            } else {
                Logger.error(this, "Failed to backup database: {}", sdDB.getName());
            }
        } catch (Exception e) {
            Logger.error(this, "Failed to back up database", e);
        }
    }
}
