package co.smartreceipts.android.model.factory;

import androidx.annotation.NonNull;

import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

    public ExchangeRateBuilderFactory setBaseCurrency(@NonNull CurrencyUnit baseCurrency) {
        _baseCurrencyCode = baseCurrency.getCode();
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

    public ExchangeRateBuilderFactory setRate(@NonNull CurrencyUnit currency, double rate) {
        return setRate(currency.getCode(), rate);
    }

    public ExchangeRateBuilderFactory setRate(@NonNull CurrencyUnit currency, @NonNull String rateString) {
        return setRate(currency.getCode(), ModelUtils.tryParse(rateString, new BigDecimal(-1)));
    }

    public ExchangeRateBuilderFactory setRate(@NonNull CurrencyUnit currency, @NonNull BigDecimal rate) {
        return setRate(currency.getCode(), rate);
    }

    @Override
    @NonNull
    public ExchangeRate build() {
        return new ExchangeRate(_baseCurrencyCode, _rates);
    }
}
