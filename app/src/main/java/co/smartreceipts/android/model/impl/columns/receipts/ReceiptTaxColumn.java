package co.smartreceipts.android.model.impl.columns.receipts;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptTaxColumn extends AbstractColumnImpl<Receipt> {

    public ReceiptTaxColumn(int id, @NonNull String name, @NonNull SyncState syncState, long customOrderId) {
        super(id, name, syncState, customOrderId);
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getTax().getDecimalFormattedPrice();
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<Receipt> receipts) {
        if (!receipts.isEmpty()) {
            final PriceCurrency tripCurrency = receipts.get(0).getTrip().getTripCurrency();
            final List<Price> prices = new ArrayList<>();
            for (final Receipt receipt : receipts) {
                prices.add(receipt.getTax());
            }
            return new PriceBuilderFactory().setPrices(prices, tripCurrency).build().getDecimalFormattedPrice();
        } else {
            return "";
        }
    }

}
