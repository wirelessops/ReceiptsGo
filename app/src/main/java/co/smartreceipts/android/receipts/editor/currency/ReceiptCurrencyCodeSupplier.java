package co.smartreceipts.android.receipts.editor.currency;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.currency.widget.CurrencyCodeSupplier;
import co.smartreceipts.android.fragments.ReceiptInputCache;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

/**
 * An implementation of the {@link CurrencyCodeSupplier} contract for {@link Receipt} editing
 */
public class ReceiptCurrencyCodeSupplier implements CurrencyCodeSupplier {

    private final Trip trip;
    private final ReceiptInputCache receiptInputCache;
    private final Receipt receipt;

    /**
     * Default constructor for this class
     *
     * @param trip the parent {@link Trip} instance
     * @param receiptInputCache the {@link ReceiptInputCache} that holds past input values
     * @param receipt the {@link Receipt} that we're editing or {@code null} if it's a new entry
     */
    public ReceiptCurrencyCodeSupplier(@NonNull Trip trip, @NonNull ReceiptInputCache receiptInputCache, @Nullable Receipt receipt) {
        this.trip = Preconditions.checkNotNull(trip);
        this.receiptInputCache = Preconditions.checkNotNull(receiptInputCache);
        this.receipt = receipt;
    }

    @NonNull
    @Override
    public String get() {
        if (receipt != null) {
            return receipt.getPrice().getCurrencyCode();
        } else if (receiptInputCache.getCachedCurrency() != null) {
            return receiptInputCache.getCachedCurrency();
        } else {
            return trip.getDefaultCurrencyCode();
        }
    }
}
