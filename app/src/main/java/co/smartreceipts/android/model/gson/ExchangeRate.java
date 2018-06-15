package co.smartreceipts.android.model.gson;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * <p>
 * Tracks the exchange rate from a base currency to a set of other currencies. The base currency
 * is defined in {@link #getBaseCurrency()} and can be exchanged to another currency via the rate
 * from {@link #getExchangeRate(String)} or {@link #getExchangeRate(PriceCurrency)}.
 * </p>
 * <p>
 * For example, if you had a price defined in "EUR" (the base currency) and wished to exchange it
 * to "USD", you could call {@link #getExchangeRate(String)} to find the exchange rate between the two (i.e
 * the numbed for which the "EUR" price needs to be multiplied by to convert to "USD").
 * </p>
 */
public class ExchangeRate implements Serializable {

    public static final int PRECISION = 6;

    private final String base;
    private final Map<String, Double> rates;

    public ExchangeRate(String base, Map<String, Double> rates) {
        this.base = base;
        this.rates = rates;
    }

    /**
     * Gets the base currency for this exchange rate. All exchange rates will be compared against this.
     *
     * @return the base {@link PriceCurrency}
     */
    @Nullable
    public PriceCurrency getBaseCurrency() {
        if (base == null) {
            return null;
        } else {
            return PriceCurrency.getInstance(base);
        }
    }

    /**
     * Gets the base currency code for this exchange rate. All exchange rates will be compared against this.
     *
     * @return the base currency code as a {@link String}
     */
    @Nullable
    public String getBaseCurrencyCode() {
        return base;
    }

    /**
     * Tests if this exchange rate is properly defined in order to support an exchange rate for a given currency
     *
     * @param currency the {@link PriceCurrency} to test if we have a valid rate
     * @return {@code true} if we have a valid exchange rate. {@code false} otherwise
     */
    public boolean supportsExchangeRateFor(@NonNull PriceCurrency currency) {
        return supportsExchangeRateFor(currency.getCurrencyCode());
    }

    /**
     * Tests if this exchange rate is properly defined in order to support an exchange rate for a given currency
     *
     * @param currencyCode the currency code (e.g. "USD")to test if we have a valid rate
     * @return {@code true} if we have a valid exchange rate or this is the same currency as the base one. {@code false} otherwise
     */
    public boolean supportsExchangeRateFor(@NonNull String currencyCode) {
        if (currencyCode.equalsIgnoreCase(base)) {
            // We always support same currency exchange (i.e. "USD" -> "USD")
            return true;
        }
        if (base == null || rates == null) {
            return false;
        } else {
            if (rates.containsKey(currencyCode)) {
                return rates.get(currencyCode) > 0;
            } else {
                return false;
            }
        }
    }

    /**
     * Gets the exchange rate from the base currency to a currency of your choice
     *
     * @param exchangeCurrency the {@link PriceCurrency} to exchange to
     * @return the exchange rate or {@code null} if we did not define one for this currency
     */
    @Nullable
    public BigDecimal getExchangeRate(@NonNull PriceCurrency exchangeCurrency) {
        return getExchangeRate(exchangeCurrency.getCurrencyCode());
    }

    /**
     * Gets the exchange rate from the base currency to a currency of your choice
     *
     * @param exchangeCurrencyCode the currency code (e.g. "USD") to exchange to
     * @return the exchange rate or {@code null} if we did not define one for this currency code. If the
     * desired currency is the same as base currency, the "1" will be returned.
     */
    @Nullable
    public BigDecimal getExchangeRate(@NonNull String exchangeCurrencyCode) {
        if (exchangeCurrencyCode.equalsIgnoreCase(base)) {
            // We always support same currency exchange (i.e. "USD" -> "USD")
            return BigDecimal.ONE;
        }
        if (supportsExchangeRateFor(exchangeCurrencyCode)) {
            return BigDecimal.valueOf(rates.get(exchangeCurrencyCode));
        } else {
            return null;
        }
    }

    /**
     * A "decimal-formatted" price, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2"
     *
     * @param exchangeCurrency the {@link PriceCurrency} to exchange to
     * @return the decimal exchange rate or an empty string if we did not define one for this currency
     */
    @NonNull
    public String getDecimalFormattedExchangeRate(@NonNull PriceCurrency exchangeCurrency) {
        return getDecimalFormattedExchangeRate(exchangeCurrency.getCurrencyCode());
    }

    /**
     * Gets the exchange rate from the base currency to a currency of your choice
     *
     * @param exchangeCurrencyCode the currency code (e.g. "USD") to exchange to
     * @return the exchange rate or {@code null} if we did not define one for this currency code. If the
     * desired currency is the same as base currency, the "1" will be returned.
     */
    @NonNull
    public String getDecimalFormattedExchangeRate(@NonNull String exchangeCurrencyCode) {
        if (exchangeCurrencyCode.equalsIgnoreCase(base)) {
            // We always support same currency exchange (i.e. "USD" -> "USD")
            return ModelUtils.getDecimalFormattedValue(BigDecimal.ONE, PRECISION);
        }
        if (supportsExchangeRateFor(exchangeCurrencyCode)) {
            return ModelUtils.getDecimalFormattedValue(BigDecimal.valueOf(rates.get(exchangeCurrencyCode)), PRECISION);
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return "ExchangeRate{" + "base='" + base + '\'' + ", rates=" + rates + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeRate)) return false;

        ExchangeRate that = (ExchangeRate) o;

        if (base != null ? !base.equals(that.base) : that.base != null) return false;
        return rates != null ? rates.equals(that.rates) : that.rates == null;
    }

    @Override
    public int hashCode() {
        int result = base != null ? base.hashCode() : 0;
        result = 31 * result + (rates != null ? rates.hashCode() : 0);
        return result;
    }
}
