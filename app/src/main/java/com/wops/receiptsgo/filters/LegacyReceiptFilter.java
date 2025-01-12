package com.wops.receiptsgo.filters;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;

/**
 * Defines a default filter implementation that is based on the legacy approach to filtering (i.e. settings)
 */
public final class LegacyReceiptFilter extends AndFilter<Receipt> {

    public LegacyReceiptFilter(@NonNull UserPreferenceManager preferences) {
        and(new LegacyReceiptMinimumPriceFilter(preferences.get(UserPreference.Receipts.MinimumReceiptPrice)));
        if (preferences.get(UserPreference.Receipts.OnlyIncludeReimbursable)) {
            and(new ReceiptIsReimbursableFilter());
        }
    }

    @Override
    Filter<Receipt> getFilter(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("This method is not supported by this class");
    }
}
