package co.smartreceipts.android.persistence.database.controllers.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;

/**
 * A simple implementation of the {@link TableEventsListener} contract that will call {@link TripTableController#get()}
 * whenever we alter on of the underlying components in order to refresh our price data.
 *
 * @param <ModelType> the model object type that this will be used to create
 */
class RefreshTripPricesListener<ModelType> implements TableEventsListener<ModelType> {

    protected final TableController<Trip> mTripTableController;

    public RefreshTripPricesListener(@NonNull TableController<Trip> tripTableController) {
        mTripTableController = Preconditions.checkNotNull(tripTableController);
    }

    @Override
    public void onGetSuccess(@NonNull List<ModelType> list) {

    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {

    }

    @Override
    public void onInsertSuccess(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mTripTableController.get();
        }
    }

    @Override
    public void onInsertFailure(@NonNull ModelType modelType, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onUpdateSuccess(@NonNull ModelType oldT, @NonNull ModelType newT, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mTripTableController.get();
        }
    }

    @Override
    public void onUpdateFailure(@NonNull ModelType oldT, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onDeleteSuccess(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mTripTableController.get();
        }
    }

    @Override
    public void onDeleteFailure(@NonNull ModelType modelType, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }
}
