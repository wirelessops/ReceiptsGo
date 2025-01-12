package com.wops.receiptsgo.receipts.editor.currency;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.currency.widget.CurrencyCodeSupplier;
import com.wops.receiptsgo.fragments.ReceiptInputCache;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;

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
