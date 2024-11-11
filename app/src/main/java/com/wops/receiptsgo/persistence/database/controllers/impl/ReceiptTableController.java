package com.wops.receiptsgo.persistence.database.controllers.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.squareup.picasso.Picasso;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.ErrorEvent;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.PersistenceManager;
import com.wops.receiptsgo.persistence.database.controllers.ReceiptTableEventsListener;
import com.wops.receiptsgo.persistence.database.controllers.TableEventsListener;
import com.wops.receiptsgo.persistence.database.controllers.alterations.ReceiptTableActionAlterations;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.analytics.log.Logger;
import dagger.Lazy;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import wb.android.storage.StorageManager;

@ApplicationScope
public class ReceiptTableController extends TripForeignKeyAbstractTableController<Receipt> {

    private final ReceiptTableActionAlterations mReceiptTableActionAlterations;
    private final CopyOnWriteArrayList<ReceiptTableEventsListener> mReceiptTableEventsListeners = new CopyOnWriteArrayList<>();

    @Inject
    public ReceiptTableController(@NonNull Context context,
                                  @NonNull PersistenceManager persistenceManager,
                                  @NonNull Analytics analytics,
                                  @NonNull TripTableController tripTableController,
                                  @NonNull Lazy<Picasso> picassoLazy) {
        this(context, persistenceManager.getDatabase().getReceiptsTable(), persistenceManager.getStorageManager(),
                analytics, tripTableController, picassoLazy);
    }

    private ReceiptTableController(@NonNull Context context, @NonNull ReceiptsTable receiptsTable, @NonNull StorageManager storageManager, @NonNull Analytics analytics, @NonNull TripTableController tripTableController, @NonNull Lazy<Picasso> picassoLazy) {
        this(receiptsTable, new ReceiptTableActionAlterations(context, receiptsTable, storageManager, picassoLazy), analytics, tripTableController);
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
}
