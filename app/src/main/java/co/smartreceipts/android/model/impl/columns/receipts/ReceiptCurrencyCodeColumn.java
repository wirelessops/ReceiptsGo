package co.smartreceipts.android.model.impl.columns.receipts;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptCurrencyCodeColumn extends AbstractColumnImpl<Receipt> {

    public ReceiptCurrencyCodeColumn(int id, @NonNull String name, @NonNull SyncState syncState, long customOrderId) {
        super(id, name, syncState, customOrderId);
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getPrice().getCurrencyCode();
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<Receipt> receipts) {
        final PriceCurrency tripCurrency = receipts.get(0).getTrip().getTripCurrency();
        return new PriceBuilderFactory().setPriceables(receipts, tripCurrency).build().getCurrencyCode();
    }
}
