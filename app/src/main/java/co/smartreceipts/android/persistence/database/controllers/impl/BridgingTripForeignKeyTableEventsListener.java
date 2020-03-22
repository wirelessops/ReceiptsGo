package co.smartreceipts.android.persistence.database.controllers.impl;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.persistence.database.controllers.TripForeignKeyTableEventsListener;
import co.smartreceipts.core.sync.model.Syncable;
import io.reactivex.Scheduler;

/**
 * A temporary class to bridge our refactoring work and avoid breaking changes while we get this all in place
 */
public class BridgingTripForeignKeyTableEventsListener<ModelType extends Keyed & Syncable> extends BridgingTableEventsListener<ModelType> {

    private final TripForeignKeyAbstractTableController<ModelType> tripForeignKeyAbstractTableController;
    private final TripForeignKeyTableEventsListener<ModelType> tripForeignKeyTableEventsListener;

    public BridgingTripForeignKeyTableEventsListener(@NonNull TripForeignKeyAbstractTableController<ModelType> tableController,
                                                     @NonNull TripForeignKeyTableEventsListener<ModelType> listener,
                                                     @NonNull Scheduler observeOnScheduler) {
        super(tableController, listener, observeOnScheduler);
        this.tripForeignKeyAbstractTableController = Preconditions.checkNotNull(tableController);
        this.tripForeignKeyTableEventsListener = Preconditions.checkNotNull(listener);
    }

    @Override
    public void subscribe() {
        super.subscribe();

        compositeDisposable.add(this.tripForeignKeyAbstractTableController.getForeignKeyGetStream()
                .observeOn(observeOnScheduler)
                .subscribe(foreignKeyGetResult -> {
                    if (foreignKeyGetResult.getThrowable() == null) {
                        //noinspection ConstantConditions
                        tripForeignKeyTableEventsListener.onGetSuccess(foreignKeyGetResult.get(), foreignKeyGetResult.getTrip());
                    } else {
                        tripForeignKeyTableEventsListener.onGetFailure(foreignKeyGetResult.getThrowable(), foreignKeyGetResult.getTrip());
                    }
                }));
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();
    }
}
