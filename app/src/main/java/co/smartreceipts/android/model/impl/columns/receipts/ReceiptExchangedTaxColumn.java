package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Converts the {@link co.smartreceipts.android.model.Receipt#getTax()} based on the current exchange rate
 */
public final class ReceiptExchangedTaxColumn extends AbstractExchangedPriceColumn {

    public ReceiptExchangedTaxColumn(int id, @NonNull String name, @NonNull SyncState syncState,
                                     @NonNull Context context, int customOrderId) {
        super(id, name, syncState, context, customOrderId);
    }

    @NonNull
    protected Price getPrice(@NonNull Receipt receipt) {
        return receipt.getTax();
    }
}
