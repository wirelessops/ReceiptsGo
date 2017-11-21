package co.smartreceipts.android.model.impl.columns.receipts;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptExchangeRateColumn extends AbstractColumnImpl<Receipt> {

    public ReceiptExchangeRateColumn(int id, @NonNull String name, @NonNull SyncState syncState, int customOrderId) {
        super(id, name, syncState, customOrderId);
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getPrice().getExchangeRate().getDecimalFormattedExchangeRate(receipt.getTrip().getTripCurrency());
    }
}
