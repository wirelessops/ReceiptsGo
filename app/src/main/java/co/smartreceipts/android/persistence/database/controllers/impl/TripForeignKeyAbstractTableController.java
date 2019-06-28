package co.smartreceipts.android.persistence.database.controllers.impl;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.TripForeignKeyTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
import co.smartreceipts.android.persistence.database.controllers.results.ForeignKeyGetResult;
import co.smartreceipts.android.persistence.database.tables.TripForeignKeyAbstractSqlTable;
import co.smartreceipts.android.sync.model.Syncable;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.SingleSubject;
import io.reactivex.subjects.Subject;


/**
 * Provides a top-level implementation of the {@link TableController} contract for {@link TripForeignKeyAbstractSqlTable}
 * instances
 *
 * @param <ModelType> the model object type that this will be used to create
 */
public class TripForeignKeyAbstractTableController<ModelType extends Keyed & Syncable> extends AbstractTableController<ModelType> {

    private final ConcurrentHashMap<TripForeignKeyTableEventsListener<ModelType>, BridgingTripForeignKeyTableEventsListener<ModelType>> mBridgingTableEventsListeners = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<TripForeignKeyTableEventsListener<ModelType>> mForeignTableEventsListeners = new CopyOnWriteArrayList<>();
    protected final TripForeignKeyAbstractSqlTable<ModelType> mTripForeignKeyTable;

    private final Subject<ForeignKeyGetResult<ModelType>> foreignKeyGetStreamSubject = PublishSubject.<ForeignKeyGetResult<ModelType>>create().toSerialized();

    public TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType> table, @NonNull Analytics analytics) {
        super(table, analytics);
        mTripForeignKeyTable = table;
    }

    public TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations, @NonNull Analytics analytics) {
        super(table, tableActionAlterations, analytics);
        mTripForeignKeyTable = table;
    }

    TripForeignKeyAbstractTableController(@NonNull TripForeignKeyAbstractSqlTable<ModelType> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations, @NonNull Analytics analytics, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        super(table, tableActionAlterations, analytics, subscribeOnScheduler, observeOnScheduler);
        mTripForeignKeyTable = table;
    }

    @Override
    public synchronized void subscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        if (tableEventsListener instanceof TripForeignKeyTableEventsListener) {
            final TripForeignKeyTableEventsListener<ModelType> tripForeignKeyTableEventsListener = (TripForeignKeyTableEventsListener<ModelType>) tableEventsListener;
            final BridgingTripForeignKeyTableEventsListener<ModelType> bridge = new BridgingTripForeignKeyTableEventsListener<ModelType>(this, tripForeignKeyTableEventsListener, mObserveOnScheduler);
            mBridgingTableEventsListeners.put(tripForeignKeyTableEventsListener, bridge);
            mForeignTableEventsListeners.add(tripForeignKeyTableEventsListener);
            bridge.subscribe();
        } else {
            super.subscribe(tableEventsListener);
        }
    }

    @Override
    public synchronized void unsubscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        if (tableEventsListener instanceof TripForeignKeyTableEventsListener) {
            final TripForeignKeyTableEventsListener<ModelType> tripForeignKeyTableEventsListener = (TripForeignKeyTableEventsListener<ModelType>) tableEventsListener;
            mForeignTableEventsListeners.remove(tripForeignKeyTableEventsListener);
            final BridgingTripForeignKeyTableEventsListener<ModelType> bridge = mBridgingTableEventsListeners.remove(tripForeignKeyTableEventsListener);
            if (bridge != null) {
                bridge.unsubscribe();
            }
        } else {
            super.unsubscribe(tableEventsListener);
        }
    }

    /**
     * Retrieves list of all objects that are stored within this table for a particular {@link Trip}
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     */
    public synchronized Single<List<ModelType>> get(@NonNull Trip trip) {
        return get(trip, true);
    }

    /**
     * Retrieves list of all objects that are stored within this table for a particular {@link Trip}
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @param isDescending {@code true} for descending order, {@code false} for ascending
     */
    public synchronized Single<List<ModelType>> get(@NonNull final Trip trip, final  boolean isDescending) {
        Logger.info(this, "#get: {}", trip);
        final SingleSubject<List<ModelType>> getSubject = SingleSubject.create();
        mTableActionAlterations.preGet()
                .subscribeOn(mSubscribeOnScheduler)
                .andThen(mTripForeignKeyTable.get(trip, isDescending))
                .flatMap(mTableActionAlterations::postGet)
                .doOnSuccess(modelTypes -> {
                    Logger.debug(TripForeignKeyAbstractTableController.this, "#onForeignKeyGetSuccess - onSuccess");
                    foreignKeyGetStreamSubject.onNext(new ForeignKeyGetResult<>(trip, modelTypes));
                })
                .doOnError(throwable -> {
                    Logger.error(TripForeignKeyAbstractTableController.this, "#onForeignKeyGetSuccess - onError");
                    mAnalytics.record(new ErrorEvent(TripForeignKeyAbstractTableController.this, throwable));
                    foreignKeyGetStreamSubject.onNext(new ForeignKeyGetResult<>(trip, throwable));
                })
                .observeOn(mObserveOnScheduler)
                .subscribe(getSubject);

        return getSubject;
    }

    @NonNull
    public Observable<ForeignKeyGetResult<ModelType>> getForeignKeyGetStream() {
        return foreignKeyGetStreamSubject;
    }
}
