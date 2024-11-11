package com.wops.receiptsgo.distance.editor.currency;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.currency.widget.CurrencyCodeSupplier;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Trip;

/**
 * An implementation of the {@link CurrencyCodeSupplier} contract for {@link Distance} editing
 */
public class DistanceCurrencyCodeSupplier implements CurrencyCodeSupplier {

    private final Trip trip;
    private final Distance distance;

    /**
     * Default constructor for this class
     *
     * @param trip the parent {@link Trip} instance
     * @param distance the {@link Distance} that we're editing or {@code null} if it's a new entry
     */
    public DistanceCurrencyCodeSupplier(@NonNull Trip trip, @Nullable Distance distance) {
        this.trip = Preconditions.checkNotNull(trip);
        this.distance = distance;
    }

    @NonNull
    @Override
    public String get() {
        if (distance != null) {
            return distance.getPrice().getCurrencyCode();
        } else {
            return trip.getDefaultCurrencyCode();
        }
    }
}
