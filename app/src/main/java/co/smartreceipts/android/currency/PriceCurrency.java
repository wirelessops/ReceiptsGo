package co.smartreceipts.android.currency;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.utils.log.Logger;

public final class PriceCurrency {

    @Deprecated
    public static final PriceCurrency MISSING_CURRENCY = new PriceCurrency("NUL");
    
    @Deprecated
    public static final PriceCurrency MIXED_CURRENCY = new PriceCurrency("MIXED");

    private static final Map<String, PriceCurrency> sCurrencyMap = new ConcurrentHashMap<>();

    private final String mCurrencyCode;
    private Currency mCurrency;

    // Saved to reduce Memory Allocs for heavy calls
    private final Map<Integer, NumberFormat> numberFormatCache = new ConcurrentHashMap<>();

    @NonNull
    public static PriceCurrency getInstance(@NonNull String currencyCode) {
        // Note: I'm not concerned if we have a few duplicate entries (ie this isn't fully thread safe) as the objects are all equal
        PriceCurrency priceCurrency = sCurrencyMap.get(currencyCode);
        if (priceCurrency != null) {
            return priceCurrency;
        } else {
            priceCurrency = new PriceCurrency(currencyCode);
            sCurrencyMap.put(currencyCode, priceCurrency);
            return priceCurrency;
        }
    }

    @NonNull
    public static PriceCurrency getDefaultCurrency() {
        try {
            return PriceCurrency.getInstance(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        } catch (IllegalArgumentException e) {
            Logger.warn(PriceCurrency.class, "Unable to find a default currency, since the device has an unsupported ISO 3166 locale. Returning USD instead");
            return PriceCurrency.getInstance("USD");
        }
    }

    private PriceCurrency(@NonNull String currencyCode) {
        this.mCurrencyCode = Preconditions.checkNotNull(currencyCode);
        try {
            mCurrency = Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            Logger.warn(this, "Unknown system currency code requested: {}. Handling this internally", currencyCode);
        }
    }

    @NonNull
    public final String getCurrencyCode() {
        if (mCurrency != null) {
            return mCurrency.getCurrencyCode();
        } else {
            return mCurrencyCode;
        }
    }

    @NonNull
    public final String format(@NonNull BigDecimal price, int decimalPrecision) {
        try {
            if (mCurrency != null) {
                NumberFormat numberFormat = numberFormatCache.get(decimalPrecision);
                if (numberFormat == null) {
                    numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                    numberFormat.setCurrency(mCurrency);
                    numberFormat.setMaximumFractionDigits(decimalPrecision);
                    numberFormat.setMinimumFractionDigits(decimalPrecision);
                    numberFormat.setGroupingUsed(false);
                    numberFormatCache.put(decimalPrecision, numberFormat);
                }
                return numberFormat.format(price);
            } else {
                return mCurrencyCode + ModelUtils.getDecimalFormattedValue(price);
            }
        } catch (NumberFormatException e) {
            return "$0.00";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceCurrency)) return false;

        PriceCurrency that = (PriceCurrency) o;

        return mCurrencyCode.equals(that.mCurrencyCode);

    }

    @Override
    public int hashCode() {
        return mCurrencyCode.hashCode();
    }

    @VisibleForTesting
    public static void clearStaticCachesForTesting() {
        sCurrencyMap.clear();
    }
}
