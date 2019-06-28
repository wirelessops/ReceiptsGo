package co.smartreceipts.android.persistence.database.controllers.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.ReceiptTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;

/**
 * A simple implementation of the {@link TableEventsListener} contract that will call {@link TripTableController#get()}
 * whenever we alter on of the underlying components in order to refresh our price data.
 */
class ReceiptRefreshTripPricesListener extends RefreshTripPricesListener<Receipt> implements ReceiptTableEventsListener {

    public ReceiptRefreshTripPricesListener(@NonNull TableController<Trip> tripTableController) {
        super(tripTableController);
    }

    @Override
    public void onMoveSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        mTripTableController.get();
    }

    @Override
    public void onMoveFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {

    }

    @Override
    public void onCopySuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        mTripTableController.get();
    }

    @Override
    public void onCopyFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {

    }

    @Override
    public void onGetSuccess(@NonNull List<Receipt> list, @NonNull Trip trip) {

    }

    @Override
    public void onGetFailure(@Nullable Throwable e, @NonNull Trip trip) {

    }
}
