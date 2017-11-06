package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * Defines an immutable implementation of the {@link co.smartreceipts.android.model.Price} interface
 * for a collection of other price objects.
 */
public final class ImmutableNetPriceImpl extends AbstractPriceImpl {

    private static final int ROUNDING_PRECISION = Price.ROUNDING_PRECISION + 2;

    private final PriceCurrency currency;
    private final BigDecimal totalPrice;
    private final BigDecimal possiblyIncorrectTotalPrice;
    private final ExchangeRate exchangeRate;
    private final boolean areAllExchangeRatesValid;
    private final Map<PriceCurrency, BigDecimal> currencyToPriceMap;

    public ImmutableNetPriceImpl(@NonNull PriceCurrency baseCurrency, @NonNull List<Price> prices) {
        this.currency = baseCurrency;
        this.currencyToPriceMap = new HashMap<>();
        BigDecimal possiblyIncorrectTotalPrice = new BigDecimal(0);
        BigDecimal totalPrice = new BigDecimal(0);
        boolean areAllExchangeRatesValid = true;
        for (final Price price : prices) {
            final BigDecimal priceToAdd;
            final PriceCurrency currencyForPriceToAdd;
            if (price.getExchangeRate().supportsExchangeRateFor(baseCurrency)) {
                priceToAdd = price.getPrice().multiply(price.getExchangeRate().getExchangeRate(baseCurrency));
                totalPrice = totalPrice.add(priceToAdd);
                currencyForPriceToAdd = baseCurrency;

            } else {
                // If not, let's just hope for the best with whatever we have to add
                priceToAdd = price.getPrice();
                currencyForPriceToAdd = price.getCurrency();
                areAllExchangeRatesValid = false;
            }
            possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.add(priceToAdd);
            final BigDecimal priceForCurrency = currencyToPriceMap.containsKey(currencyForPriceToAdd) ? currencyToPriceMap.get(currencyForPriceToAdd).add(priceToAdd) : priceToAdd;
            currencyToPriceMap.put(currencyForPriceToAdd, priceForCurrency);
        }
        this.totalPrice = totalPrice.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        this.possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        this.areAllExchangeRatesValid = areAllExchangeRatesValid;
        this.exchangeRate = new ExchangeRateBuilderFactory().setBaseCurrency(baseCurrency).build();
    }

    @SuppressWarnings("unchecked")
    private ImmutableNetPriceImpl(@NonNull Parcel in) {
        this(PriceCurrency.getInstance(in.readString()),
                (BigDecimal) in.readSerializable(),
                (BigDecimal) in.readSerializable(),
                (ExchangeRate) in.readSerializable(),
                in.readInt() > 0,
                restoreCurrencyToPriceMapFromParcel(in));
    }

    private ImmutableNetPriceImpl(@NonNull PriceCurrency currency,
                                  @NonNull BigDecimal totalPrice,
                                  @NonNull BigDecimal possiblyIncorrectTotalPrice,
                                  @NonNull ExchangeRate exchangeRate,
                                  boolean areAllExchangeRatesValid,
                                  @NonNull Map<PriceCurrency, BigDecimal> currencyToPriceMap) {
        this.currency = Preconditions.checkNotNull(currency);
        this.totalPrice = Preconditions.checkNotNull(totalPrice);
        this.possiblyIncorrectTotalPrice = Preconditions.checkNotNull(possiblyIncorrectTotalPrice);
        this.exchangeRate = Preconditions.checkNotNull(exchangeRate);
        this.areAllExchangeRatesValid = areAllExchangeRatesValid;
        this.currencyToPriceMap = Preconditions.checkNotNull(currencyToPriceMap);
    }

    @NonNull
    private static Map<PriceCurrency, BigDecimal> restoreCurrencyToPriceMapFromParcel(@NonNull Parcel in) {
        final Map<PriceCurrency, BigDecimal> currencyToPriceMap = new HashMap<>();
        final int size = in.readInt();
        for(int i = 0; i < size; i++){
            final PriceCurrency currency = PriceCurrency.getInstance(in.readString());
            final BigDecimal price = (BigDecimal) in.readSerializable();
            currencyToPriceMap.put(currency, price);
        }
        return currencyToPriceMap;
    }

    @Override
    public float getPriceAsFloat() {
        if (areAllExchangeRatesValid) {
            return totalPrice.floatValue();
        } else {
            return possiblyIncorrectTotalPrice.floatValue();
        }
    }

    @NonNull
    @Override
    public BigDecimal getPrice() {
        if (areAllExchangeRatesValid) {
            return totalPrice;
        } else {
            return possiblyIncorrectTotalPrice;
        }
    }

    @NonNull
    @Override
    public String getDecimalFormattedPrice() {
        if (areAllExchangeRatesValid) {
            return ModelUtils.getDecimalFormattedValue(totalPrice);
        } else {
            return ModelUtils.getDecimalFormattedValue(possiblyIncorrectTotalPrice);
        }
    }

    @NonNull
    @Override
    public String getCurrencyFormattedPrice() {
        if (areAllExchangeRatesValid) {
            return ModelUtils.getCurrencyFormattedValue(totalPrice, currency);
        } else {
            final List<String> currencyStrings = new ArrayList<>();
            for (PriceCurrency currency : currencyToPriceMap.keySet()) {
                currencyStrings.add(ModelUtils.getCurrencyFormattedValue(currencyToPriceMap.get(currency), currency));
            }
            return TextUtils.join("; ", currencyStrings);
        }
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        if (areAllExchangeRatesValid) {
            return ModelUtils.getCurrencyCodeFormattedValue(totalPrice, currency);
        } else {
            final List<String> currencyStrings = new ArrayList<>();
            for (PriceCurrency currency : currencyToPriceMap.keySet()) {
                currencyStrings.add(ModelUtils.getCurrencyCodeFormattedValue(currencyToPriceMap.get(currency), currency));
            }
            return TextUtils.join("; ", currencyStrings);
        }
    }

    @NonNull
    @Override
    public PriceCurrency getCurrency() {
        return currency;
    }

    @NonNull
    @Override
    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    @NonNull
    @Override
    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    public boolean areAllExchangeRatesValid() {
        // TODO: Figure out how to expose this better
        return areAllExchangeRatesValid;
    }

    @Override
    public String toString() {
        return getCurrencyFormattedPrice();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currency.getCurrencyCode());
        dest.writeSerializable(totalPrice);
        dest.writeSerializable(possiblyIncorrectTotalPrice);
        dest.writeSerializable(exchangeRate);
        dest.writeInt(areAllExchangeRatesValid ? 1 : 0);

        // Finally, write the map
        dest.writeInt(currencyToPriceMap.size());
        for(final Map.Entry<PriceCurrency, BigDecimal> entry : currencyToPriceMap.entrySet()){
            dest.writeString(entry.getKey().getCurrencyCode());
            dest.writeSerializable(entry.getValue());
        }
    }


    public static final Creator<ImmutableNetPriceImpl> CREATOR = new Creator<ImmutableNetPriceImpl>() {
        public ImmutableNetPriceImpl createFromParcel(Parcel source) {
            return new ImmutableNetPriceImpl(source);
        }

        public ImmutableNetPriceImpl[] newArray(int size) {
            return new ImmutableNetPriceImpl[size];
        }
    };
}
