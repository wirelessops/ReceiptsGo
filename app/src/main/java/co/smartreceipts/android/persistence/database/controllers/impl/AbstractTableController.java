package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.StubTableActionAlterations;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
import co.smartreceipts.android.persistence.database.controllers.results.DeleteResult;
import co.smartreceipts.android.persistence.database.controllers.results.GetResult;
import co.smartreceipts.android.persistence.database.controllers.results.InsertResult;
import co.smartreceipts.android.persistence.database.controllers.results.UpdateResult;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.sync.model.Syncable;
import co.smartreceipts.android.utils.PreFixedThreadFactory;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.SingleSubject;
import io.reactivex.subjects.Subject;


/**
 * Provides a top-level implementation of the {@link TableController} contract
 *
 * @param <ModelType> the model object type that this will be used to create
 */
abstract class AbstractTableController<ModelType extends Keyed & Syncable> implements TableController<ModelType> {

    protected final String TAG = getClass().getSimpleName();

    private final Table<ModelType> mTable;
    private final ConcurrentHashMap<TableEventsListener<ModelType>, BridgingTableEventsListener<ModelType>> mBridgingTableEventsListeners = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<TableEventsListener<ModelType>> mTableEventsListeners = new CopyOnWriteArrayList<>();
    protected final TableActionAlterations<ModelType> mTableActionAlterations;
    protected final Analytics mAnalytics;
    protected final Scheduler mSubscribeOnScheduler;
    protected final Scheduler mObserveOnScheduler;

    private final Subject<GetResult<ModelType>> getStreamSubject = PublishSubject.<GetResult<ModelType>>create().toSerialized();
    private final Subject<InsertResult<ModelType>> insertStreamSubject = PublishSubject.<InsertResult<ModelType>>create().toSerialized();
    private final Subject<UpdateResult<ModelType>> updateStreamSubject = PublishSubject.<UpdateResult<ModelType>>create().toSerialized();
    private final Subject<DeleteResult<ModelType>> deleteStreamSubject = PublishSubject.<DeleteResult<ModelType>>create().toSerialized();

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    public AbstractTableController(@NonNull Table<ModelType> table, @NonNull Analytics analytics) {
        this(table, new StubTableActionAlterations<>(), analytics);
    }

    public AbstractTableController(@NonNull Table<ModelType> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations, @NonNull Analytics analytics) {
        mTable = Preconditions.checkNotNull(table);
        mTableActionAlterations = Preconditions.checkNotNull(tableActionAlterations);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mSubscribeOnScheduler = Schedulers.from(Executors.newSingleThreadExecutor(new PreFixedThreadFactory(getClass().getSimpleName())));
        mObserveOnScheduler = AndroidSchedulers.mainThread();
    }

    AbstractTableController(@NonNull Table<ModelType> table, @NonNull TableActionAlterations<ModelType> tableActionAlterations,
                            @NonNull Analytics analytics, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        mTable = Preconditions.checkNotNull(table);
        mTableActionAlterations = Preconditions.checkNotNull(tableActionAlterations);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    @Override
    public synchronized void subscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        final BridgingTableEventsListener<ModelType> bridge = new BridgingTableEventsListener<ModelType>(this, tableEventsListener, mObserveOnScheduler);
        mBridgingTableEventsListeners.put(tableEventsListener, bridge);
        mTableEventsListeners.add(tableEventsListener);
        bridge.subscribe();
    }

    @Override
    public synchronized void unsubscribe(@NonNull TableEventsListener<ModelType> tableEventsListener) {
        mTableEventsListeners.remove(tableEventsListener);
        final BridgingTableEventsListener<ModelType> bridge = mBridgingTableEventsListeners.remove(tableEventsListener);
        if (bridge != null) {
            bridge.unsubscribe();
        }
    }

    @Override
    @NonNull
    public Single<List<ModelType>> get() {
        Logger.info(this, "#get");

        final SingleSubject<List<ModelType>> getSubject = SingleSubject.create();
        mTableActionAlterations.preGet()
                .subscribeOn(mSubscribeOnScheduler)
                .andThen(mTable.get())
                .flatMap(mTableActionAlterations::postGet)
                .doOnSuccess(modelTypes -> {
                    Logger.debug(AbstractTableController.this, "#onGetSuccess - onNext");
                    getStreamSubject.onNext(new GetResult<>(modelTypes));
                })
                .doOnError(throwable -> {
                    Logger.error(AbstractTableController.this, "#onGetFailure - onError", throwable);
                    mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                    getStreamSubject.onNext(new GetResult<>(throwable));
                })
                .onErrorReturnItem(Collections.emptyList())
                .subscribe(getSubject);

        return getSubject;
    }

    @NonNull
    @Override
    public Observable<GetResult<ModelType>> getStream() {
        return getStreamSubject;
    }

    @Override
    public void insert(@NonNull final ModelType modelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#insert: {}", modelType);

        mTableActionAlterations.preInsert(modelType)
                .subscribeOn(mSubscribeOnScheduler)
                .flatMap(insertedItem -> mTable.insert(insertedItem, databaseOperationMetadata))
                .flatMap(mTableActionAlterations::postInsert)
                .doOnSuccess(insertedItem -> {
                    Logger.debug(AbstractTableController.this, "#onInsertSuccess - onNext");
                    insertStreamSubject.onNext(new InsertResult<>(insertedItem, databaseOperationMetadata));
                })
                .doOnError(throwable -> {
                    Logger.error(AbstractTableController.this, "#onInsertFailure - onError", throwable);
                    mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                    insertStreamSubject.onNext(new InsertResult<>(modelType, throwable, databaseOperationMetadata));
                })
                .map(Optional::of)
                .onErrorReturnItem(Optional.absent())
                .subscribe();
    }

    @NonNull
    @Override
    public Observable<InsertResult<ModelType>> insertStream() {
        return insertStreamSubject;
    }

    @NonNull
    @Override
    public Observable<Optional<ModelType>> update(@NonNull final ModelType oldModelType, @NonNull ModelType newModelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#update: {}; {}", oldModelType, newModelType);
        final Subject<Optional<ModelType>> updateSubject = PublishSubject.create();

        mTableActionAlterations.preUpdate(oldModelType, newModelType)
                .flatMap(updatedItem -> mTable.update(oldModelType, updatedItem, databaseOperationMetadata))
                .flatMap(modelType -> mTableActionAlterations.postUpdate(oldModelType, modelType))
                .doOnSuccess(updatedItem -> {
                    Logger.debug(AbstractTableController.this, "#onUpdateSuccess - onNext");
                    updateStreamSubject.onNext(new UpdateResult<>(oldModelType, updatedItem, databaseOperationMetadata));
                })
                .doOnError(throwable -> {
                    Logger.error(AbstractTableController.this, "#onUpdateFailure - onError", throwable);
                    mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                    updateStreamSubject.onNext(new UpdateResult<>(oldModelType, null, throwable, databaseOperationMetadata));
                })
                .map(Optional::of)
                .onErrorReturnItem(Optional.absent())
                .toObservable()
                .subscribe(updateSubject);

        return updateSubject;
    }

    @NonNull
    @Override
    public Observable<UpdateResult<ModelType>> updateStream() {
        return updateStreamSubject;
    }

    @Override
    public synchronized void delete(@NonNull final ModelType modelType, @NonNull final DatabaseOperationMetadata databaseOperationMetadata) {
        Logger.info(this, "#delete: {}", modelType);

        mTableActionAlterations.preDelete(modelType)
                .subscribeOn(mSubscribeOnScheduler)
                .flatMap(deletedItem -> mTable.delete(deletedItem, databaseOperationMetadata))
                .flatMap(mTableActionAlterations::postDelete)
                .doOnSuccess(deletedItem -> {
                    Logger.debug(AbstractTableController.this, "#onDeleteSuccess - onNext");
                    deleteStreamSubject.onNext(new DeleteResult<>(deletedItem, databaseOperationMetadata));
                })
                .doOnError(throwable -> {
                    Logger.error(AbstractTableController.this, "#onDeleteFailure - onError", throwable);
                    mAnalytics.record(new ErrorEvent(AbstractTableController.this, throwable));
                    deleteStreamSubject.onNext(new DeleteResult<>(modelType, throwable, databaseOperationMetadata));
                })
                .map(Optional::of)
                .onErrorReturnItem(Optional.absent())
                .subscribe();
    }

    @NonNull
    @Override
    public Observable<DeleteResult<ModelType>> deleteStream() {
        return deleteStreamSubject;
    }

    protected void unsubscribeReference(@NonNull AtomicReference<Disposable> disposableReference) {
        final Disposable disposable = disposableReference.get();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

}
