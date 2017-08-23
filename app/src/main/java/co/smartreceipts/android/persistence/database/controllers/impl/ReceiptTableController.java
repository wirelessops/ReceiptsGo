package co.smartreceipts.android.persistence.database.controllers.impl;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.ReceiptTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.ReceiptTableActionAlterations;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import wb.android.storage.StorageManager;

@ApplicationScope
public class ReceiptTableController extends TripForeignKeyAbstractTableController<Receipt> {

    private final ReceiptTableActionAlterations mReceiptTableActionAlterations;
    private final CopyOnWriteArrayList<ReceiptTableEventsListener> mReceiptTableEventsListeners = new CopyOnWriteArrayList<>();

    @Inject
    public ReceiptTableController(Context context, PersistenceManager persistenceManager, Analytics analytics,
                                  TripTableController tripTableController) {
        this(context, persistenceManager.getDatabase().getReceiptsTable(), persistenceManager.getStorageManager(),
                analytics, tripTableController);
    }

    private ReceiptTableController(@NonNull Context context, @NonNull ReceiptsTable receiptsTable, @NonNull StorageManager storageManager, @NonNull Analytics analytics, @NonNull TripTableController tripTableController) {
        this(receiptsTable, new ReceiptTableActionAlterations(context, receiptsTable, storageManager), analytics, tripTableController);
    }

    private ReceiptTableController(@NonNull ReceiptsTable receiptsTable, @NonNull ReceiptTableActionAlterations receiptTableActionAlterations, @NonNull Analytics analytics, @NonNull TripTableController tripTableController) {
        super(receiptsTable, receiptTableActionAlterations, analytics);
        mReceiptTableActionAlterations = Preconditions.checkNotNull(receiptTableActionAlterations);
        subscribe(new ReceiptRefreshTripPricesListener(Preconditions.checkNotNull(tripTableController)));
    }

    ReceiptTableController(@NonNull ReceiptsTable receiptsTable, @NonNull ReceiptTableActionAlterations receiptTableActionAlterations, @NonNull Analytics analytics,
                           @NonNull TripTableController tripTableController, @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        super(receiptsTable, receiptTableActionAlterations, analytics, subscribeOnScheduler, observeOnScheduler);
        mReceiptTableActionAlterations = Preconditions.checkNotNull(receiptTableActionAlterations);
        subscribe(new ReceiptRefreshTripPricesListener(Preconditions.checkNotNull(tripTableController)));
    }

    @Override
    public synchronized void subscribe(@NonNull TableEventsListener<Receipt> tableEventsListener) {
        super.subscribe(tableEventsListener);
        if (tableEventsListener instanceof ReceiptTableEventsListener) {
            mReceiptTableEventsListeners.add((ReceiptTableEventsListener) tableEventsListener);
        }
    }

    @Override
    public synchronized void unsubscribe(@NonNull TableEventsListener<Receipt> tableEventsListener) {
        if (tableEventsListener instanceof ReceiptTableEventsListener) {
            mReceiptTableEventsListeners.remove(tableEventsListener);
        }
        super.unsubscribe(tableEventsListener);
    }

    public synchronized void move(@NonNull final Receipt receiptToMove, @NonNull Trip toTrip) {
        Logger.info(this, "#move: {}; {}", receiptToMove, toTrip);
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        final Disposable disposable = mReceiptTableActionAlterations.preMove(receiptToMove, toTrip)
                .flatMap(receipt -> mTripForeignKeyTable.insert(receipt, new DatabaseOperationMetadata()))
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .doOnSuccess(receipt -> {
                    try {
                        mReceiptTableActionAlterations.postMove(receiptToMove, receipt);
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .subscribe(receipt -> {
                    Logger.debug(this, "#onMoveSuccess - onSuccess");
                    for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                        tableEventsListener.onMoveSuccess(receiptToMove, receipt);
                    }
                    unsubscribeReference(disposableRef);
                }, throwable -> {
                    mAnalytics.record(new ErrorEvent(ReceiptTableController.this, throwable));
                    Logger.debug(this, "#onMoveFailure - onError");
                    for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                        tableEventsListener.onMoveFailure(receiptToMove, throwable);
                    }
                    unsubscribeReference(disposableRef);
                });

        disposableRef.set(disposable);
        compositeDisposable.add(disposable);
    }

    public synchronized void copy(@NonNull final Receipt receiptToCopy, @NonNull Trip toTrip) {
        Logger.info(this, "#move: {}; {}", receiptToCopy, toTrip);
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        final Disposable disposable = mReceiptTableActionAlterations.preCopy(receiptToCopy, toTrip)
                .flatMap(receipt -> mTripForeignKeyTable.insert(receipt, new DatabaseOperationMetadata()))
                .doOnSuccess(newReceipt -> {
                    try {
                        mReceiptTableActionAlterations.postCopy(receiptToCopy, newReceipt);
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(newReceipt -> {
                    Logger.debug(this, "#onCopySuccess - onSuccess");
                    for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                        tableEventsListener.onCopySuccess(receiptToCopy, newReceipt);
                    }
                    unsubscribeReference(disposableRef);
                }, throwable -> {
                    mAnalytics.record(new ErrorEvent(ReceiptTableController.this, throwable));
                    Logger.debug(this, "#onCopyFailure - onError");
                    for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                        tableEventsListener.onCopyFailure(receiptToCopy, throwable);
                    }
                    unsubscribeReference(disposableRef);
                });
        disposableRef.set(disposable);
        compositeDisposable.add(disposable);
    }

    public synchronized void swapUp(@NonNull final Receipt receiptToSwapUp) {
        Logger.info(TAG, "#swapUp: {}", receiptToSwapUp);
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        final Disposable disposable = mTripForeignKeyTable.get(receiptToSwapUp.getTrip())
                .flatMapObservable(receipts -> mReceiptTableActionAlterations.getReceiptsToSwapUp(receiptToSwapUp, receipts))
                .flatMapSingle(this::swapReceiptsSingle)
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(success -> {
                    if (success) {
                        Logger.debug(this, "#onSwapUpSuccess - onNext");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onSwapSuccess();
                        }
                    } else {
                        Logger.debug(this, "#onSwapUpFailure - onNext");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onSwapFailure(null);
                        }
                    }
                }, throwable -> {
                    mAnalytics.record(new ErrorEvent(ReceiptTableController.this, throwable));
                    Logger.debug(this, "#onSwapUpFailure - onError");
                    for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                        tableEventsListener.onSwapFailure(throwable);
                    }
                    unsubscribeReference(disposableRef);
                }, () -> {
                    Logger.debug(this, "#swapUp - onComplete");
                    unsubscribeReference(disposableRef);
                });

        disposableRef.set(disposable);
        compositeDisposable.add(disposable);
    }

    public synchronized void swapDown(@NonNull final Receipt receiptToSwapDown) {
        Logger.info(this, "#swapDown: {}", receiptToSwapDown);
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        final Disposable disposable = mTripForeignKeyTable.get(receiptToSwapDown.getTrip())
                .flatMapObservable(receipts -> mReceiptTableActionAlterations.getReceiptsToSwapDown(receiptToSwapDown, receipts))
                .flatMapSingle(this::swapReceiptsSingle)
                .subscribeOn(mSubscribeOnScheduler)
                .observeOn(mObserveOnScheduler)
                .subscribe(success -> {
                    if (success) {
                        Logger.debug(this, "#onSwapDownSuccess - onNext");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onSwapSuccess();
                        }
                    } else {
                        Logger.debug(this, "#onSwapDownFailure - onNext");
                        for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                            tableEventsListener.onSwapFailure(null);
                        }
                    }
                }, throwable -> {
                    mAnalytics.record(new ErrorEvent(ReceiptTableController.this, throwable));
                    Logger.debug(this, "#onSwapDownFailure - onError");
                    for (final ReceiptTableEventsListener tableEventsListener : mReceiptTableEventsListeners) {
                        tableEventsListener.onSwapFailure(throwable);
                    }
                    unsubscribeReference(disposableRef);
                }, () -> {
                    Logger.debug(this, "#swapDown - onComplete");
                    unsubscribeReference(disposableRef);
                });

        disposableRef.set(disposable);
        compositeDisposable.add(disposable);
    }

    private Single<Boolean> swapReceiptsSingle(final List<? extends Map.Entry<Receipt, Receipt>> entries) {
        return Observable.fromIterable(entries)
                .flatMap(entry -> update(entry.getKey(), entry.getValue(), new DatabaseOperationMetadata()))
                .filter(Optional::isPresent)
                .toList()
                .flatMap(updatedReceipts -> Single.just(entries.size() == updatedReceipts.size()));
    }
}
