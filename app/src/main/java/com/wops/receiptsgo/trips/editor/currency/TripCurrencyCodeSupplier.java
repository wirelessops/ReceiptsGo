package com.wops.receiptsgo.trips.editor.currency;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wops.receiptsgo.currency.widget.CurrencyCodeSupplier;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;

/**
 * An implementation of the {@link CurrencyCodeSupplier} contract for {@link Trip} editing
 */
public class TripCurrencyCodeSupplier implements CurrencyCodeSupplier {

    private final UserPreferenceManager userPreferenceManager;
    private final Trip trip;

    /**
     * Default constructor for this class
     *
     * @param userPreferenceManager the {@link UserPreferenceManager} for determining the system-default currency
     * @param trip the {@link Trip} that we're editing or {@code null} if it's a new entry
     */
    public TripCurrencyCodeSupplier(@NonNull UserPreferenceManager userPreferenceManager, @Nullable Trip trip) {
        this.userPreferenceManager = userPreferenceManager;
        this.trip = trip;
    }

    @NonNull
    @Override
    public String get() {
        if (trip != null) {
            return trip.getDefaultCurrencyCode();
        } else {
            return userPreferenceManager.get(UserPreference.General.DefaultCurrency);
        }
    }
}
