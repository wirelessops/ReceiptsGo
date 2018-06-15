package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;


/**
 * A temporary class to bridge our refactoring work and avoid breaking changes while we get this all in place
 */
public class BridgingTableEventsListener<ModelType> {

    private final TableController<ModelType> tableController;
    private final TableEventsListener<ModelType> listener;
    protected final Scheduler observeOnScheduler;
    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BridgingTableEventsListener(@NonNull TableController<ModelType> tableController, @NonNull TableEventsListener<ModelType> listener,
                                       @NonNull Scheduler observeOnScheduler) {
        this.tableController = Preconditions.checkNotNull(tableController);
        this.listener = Preconditions.checkNotNull(listener);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    @CallSuper
    public void subscribe() {
        compositeDisposable.add(this.tableController.getStream()
                .observeOn(observeOnScheduler)
                .subscribe(modelTypeGetResult -> {
                    if (modelTypeGetResult.getThrowable() == null) {
                        //noinspection ConstantConditions
                        listener.onGetSuccess(modelTypeGetResult.get());
                    } else {
                        listener.onGetFailure(modelTypeGetResult.getThrowable());
                    }
                }));

        compositeDisposable.add(this.tableController.insertStream()
                .observeOn(observeOnScheduler)
                .subscribe(modelTypeInsertResult -> {
                    if (modelTypeInsertResult.getThrowable() == null) {
                        if (modelTypeInsertResult.getDatabaseOperationMetadata().getOperationFamilyType() != OperationFamilyType.Silent) {
                            listener.onInsertSuccess(modelTypeInsertResult.get(), modelTypeInsertResult.getDatabaseOperationMetadata());
                        }
                    } else {
                        listener.onInsertFailure(modelTypeInsertResult.get(), modelTypeInsertResult.getThrowable(), modelTypeInsertResult.getDatabaseOperationMetadata());
                    }
                }));

        compositeDisposable.add(this.tableController.updateStream()
                .observeOn(observeOnScheduler)
                .subscribe(modelTypeUpdateResult -> {
                    if (modelTypeUpdateResult.getThrowable() == null) {
                        if (modelTypeUpdateResult.getDatabaseOperationMetadata().getOperationFamilyType() != OperationFamilyType.Silent) {
                            //noinspection ConstantConditions
                            listener.onUpdateSuccess(modelTypeUpdateResult.getOld(), modelTypeUpdateResult.getNew(), modelTypeUpdateResult.getDatabaseOperationMetadata());
                        }
                    } else {
                        listener.onUpdateFailure(modelTypeUpdateResult.getOld(), modelTypeUpdateResult.getThrowable(), modelTypeUpdateResult.getDatabaseOperationMetadata());
                    }
                }));

        compositeDisposable.add(this.tableController.deleteStream()
                .observeOn(observeOnScheduler)
                .subscribe(modelTypeDeleteResult -> {
                    if (modelTypeDeleteResult.getThrowable() == null) {
                        listener.onDeleteSuccess(modelTypeDeleteResult.get(), modelTypeDeleteResult.getDatabaseOperationMetadata());
                    } else {
                        listener.onDeleteFailure(modelTypeDeleteResult.get(), modelTypeDeleteResult.getThrowable(), modelTypeDeleteResult.getDatabaseOperationMetadata());
                    }
                }));
    }

    @CallSuper
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
