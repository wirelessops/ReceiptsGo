package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * A {@link ExchangeRate} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link ExchangeRate} objects
 */
public final class ExchangeRateBuilderFactory implements BuilderFactory<ExchangeRate> {

    private final Map<String, Double> _rates;
    private String _baseCurrencyCode;

    public ExchangeRateBuilderFactory() {
        _rates = new HashMap<>();
    }

    public ExchangeRateBuilderFactory setBaseCurrency(@NonNull PriceCurrency baseCurrency) {
        _baseCurrencyCode = baseCurrency.getCurrencyCode();
        return this;
    }

    public ExchangeRateBuilderFactory setBaseCurrency(@NonNull String baseCurrencyCode) {
        _baseCurrencyCode = baseCurrencyCode;
        return this;
    }

    public ExchangeRateBuilderFactory setRate(@NonNull String currencyCode, double rate) {
        if (rate > 0) {
            _rates.put(currencyCode, rate);
        }
        return this;
    }

    public ExchangeRateBuilderFactory setRate(@NonNull String currencyCode, @NonNull BigDecimal rate) {
        return setRate(currencyCode, rate.doubleValue());
    }

    public ExchangeRateBuilderFactory setRate(@NonNull String currencyCode, @NonNull String rateString) {
        return setRate(currencyCode, ModelUtils.tryParse(rateString, new BigDecimal(-1)));
    }

    public ExchangeRateBuilderFactory setRate(@NonNull PriceCurrency currency, double rate) {
        return setRate(currency.getCurrencyCode(), rate);
    }

    public ExchangeRateBuilderFactory setRate(@NonNull PriceCurrency currency, @NonNull BigDecimal rate) {
        return setRate(currency.getCurrencyCode(), rate.doubleValue());
    }

    public ExchangeRateBuilderFactory setRate(@NonNull PriceCurrency currency, @NonNull String rateString) {
        return setRate(currency.getCurrencyCode(), ModelUtils.tryParse(rateString, new BigDecimal(-1)));
    }

    @Override
    @NonNull
    public ExchangeRate build() {
        return new ExchangeRate(_baseCurrencyCode, _rates);
    }
}
