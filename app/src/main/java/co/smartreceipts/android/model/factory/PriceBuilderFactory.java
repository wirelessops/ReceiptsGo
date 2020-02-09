package co.smartreceipts.android.model.factory;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Priceable;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.ImmutableLegacyNetPriceImpl;
import co.smartreceipts.android.model.impl.ImmutableNetPriceImpl;
import co.smartreceipts.android.model.impl.ImmutablePriceImpl;
import co.smartreceipts.android.model.impl.LegacyTripPriceImpl;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * A {@link co.smartreceipts.android.model.Price} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Price} objects
 */
public final class PriceBuilderFactory implements BuilderFactory<Price> {

    private BigDecimal priceDecimal;
    private ExchangeRate exchangeRate;
    private PriceCurrency currency;
    private List<Priceable> priceables;
    private List<Price> prices;
    private int decimalPrecision = Price.TOTAL_DECIMAL_PRECISION;

    public PriceBuilderFactory() {
        currency = PriceCurrency.getDefaultCurrency();
        priceDecimal = BigDecimal.ZERO;
    }

    public PriceBuilderFactory(@NonNull Price price) {
        priceDecimal = price.getPrice();
        currency = price.getCurrency();
        exchangeRate = price.getExchangeRate();
    }

    @NonNull
    public PriceBuilderFactory setPrice(Price price) {
        priceDecimal = price.getPrice();
        currency = price.getCurrency();
        exchangeRate = price.getExchangeRate();
        return this;
    }

    @NonNull
    public PriceBuilderFactory setPrice(String price) {
        priceDecimal = ModelUtils.tryParse(price);
        return this;
    }

    @NonNull
    public PriceBuilderFactory setPrice(double price) {
        priceDecimal = BigDecimal.valueOf(price);
        return this;
    }

    @NonNull
    public PriceBuilderFactory setPrice(BigDecimal price) {
        priceDecimal = price;
        return this;
    }

    @NonNull
    public PriceBuilderFactory setCurrency(PriceCurrency currency) {
        this.currency = currency;
        return this;
    }

    @NonNull
    public PriceBuilderFactory setCurrency(String currencyCode) {
        currency = PriceCurrency.getInstance(currencyCode);
        return this;
    }

    @NonNull
    public PriceBuilderFactory setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
        return this;
    }

    @NonNull
    public PriceBuilderFactory setPrices(@NonNull List<? extends Price> prices, @NonNull PriceCurrency desiredCurrency) {
        this.prices = new ArrayList<>(prices);
        currency = desiredCurrency;
        return this;
    }

    @NonNull
    public PriceBuilderFactory setPriceables(@NonNull List<? extends Priceable> priceables, @NonNull PriceCurrency desiredCurrency) {
        this.priceables = new ArrayList<>(priceables);
        currency = desiredCurrency;
        return this;
    }

    @NonNull
    public PriceBuilderFactory setDecimalPrecision(int decimalPrecision) {
        this.decimalPrecision = decimalPrecision;
        return this;
    }

    @NonNull
    @Override
    public Price build() {
        if (prices != null && !prices.isEmpty()) {
            if (currency != null) {
                return new ImmutableNetPriceImpl(currency, prices);
            } else {
                return new ImmutableLegacyNetPriceImpl(prices);
            }
        }
        else if (priceables != null && !priceables.isEmpty()) {
            final int size = priceables.size();
            final ArrayList<Price> actualPrices = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                actualPrices.add(priceables.get(i).getPrice());
            }
            if (currency != null) {
                return new ImmutableNetPriceImpl(currency, actualPrices);
            } else {
                return new ImmutableLegacyNetPriceImpl(actualPrices);
            }
        }
        else {
            final BigDecimal price = priceDecimal != null ? priceDecimal : BigDecimal.ZERO;
            if (currency != null) {
                if (exchangeRate != null) {
                    return new ImmutablePriceImpl(price, currency, exchangeRate, decimalPrecision);
                } else {
                    return new ImmutablePriceImpl(price, currency, new ExchangeRateBuilderFactory().setBaseCurrency(currency).build(), decimalPrecision);
                }
            }
            else {
                return new LegacyTripPriceImpl(price, null);
            }
        }
    }

}
