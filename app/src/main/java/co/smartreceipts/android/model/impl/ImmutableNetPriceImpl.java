package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * Defines an immutable implementation of the {@link co.smartreceipts.android.model.Price} interface
 * for a collection of other price objects.
 */
public final class ImmutableNetPriceImpl extends AbstractPriceImpl {

    private final PriceCurrency currency;
    private final BigDecimal totalPrice;
    private final BigDecimal possiblyIncorrectTotalPrice;
    private final ExchangeRate exchangeRate;
    private final boolean areAllExchangeRatesValid;
    private final Map<PriceCurrency, BigDecimal> currencyToPriceMap;
    private final Map<PriceCurrency, BigDecimal> notExchangedPriceMap;
    private final String decimalFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyCodeFormattedPrice; // Note: We create/cache this as it's common, slower operation

    public ImmutableNetPriceImpl(@NonNull PriceCurrency baseCurrency, @NonNull List<Price> prices) {
        this.currency = baseCurrency;
        this.currencyToPriceMap = new HashMap<>();
        this.notExchangedPriceMap = new HashMap<>();
        BigDecimal possiblyIncorrectTotalPrice = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        boolean areAllExchangeRatesValid = true;
        for (final Price price : prices) {

            notExchangedPriceMap.put(price.getCurrency(), notExchangedPriceMap.containsKey(price.getCurrency()) ?
                    notExchangedPriceMap.get(price.getCurrency()).add(price.getPrice()) : price.getPrice());


            final BigDecimal priceToAdd;
            final PriceCurrency currencyForPriceToAdd;
            if (price.getExchangeRate().supportsExchangeRateFor(baseCurrency)) {
                priceToAdd = price.getPrice().multiply(price.getExchangeRate().getExchangeRate(baseCurrency))
                        .setScale(DEFAULT_DECIMAL_PRECISION, RoundingMode.HALF_UP);
                totalPrice = totalPrice.add(priceToAdd);
                currencyForPriceToAdd = baseCurrency;

            } else {
                // If not, let's just hope for the best with whatever we have to add
                priceToAdd = price.getPrice().setScale(DEFAULT_DECIMAL_PRECISION, RoundingMode.HALF_UP);
                currencyForPriceToAdd = price.getCurrency();
                areAllExchangeRatesValid = false;
            }
            possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.add(priceToAdd);
            final BigDecimal priceForCurrency = currencyToPriceMap.containsKey(currencyForPriceToAdd) ? currencyToPriceMap.get(currencyForPriceToAdd).add(priceToAdd) : priceToAdd;
            currencyToPriceMap.put(currencyForPriceToAdd, priceForCurrency);
        }
        this.totalPrice = totalPrice;
        this.possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice;
        this.areAllExchangeRatesValid = areAllExchangeRatesValid;
        this.exchangeRate = new ExchangeRateBuilderFactory().setBaseCurrency(baseCurrency).build();

        // Note: The actual model utils stuff is somewhat slow due to the NumberFormats behind the scenes. We pre-cache here
        this.decimalFormattedPrice = calculateDecimalFormattedPrice();
        this.currencyFormattedPrice = calculateCurrencyFormattedPrice();
        this.currencyCodeFormattedPrice = calculateCurrencyCodeFormattedPrice();
    }

    @SuppressWarnings("unchecked")
    private ImmutableNetPriceImpl(@NonNull Parcel in) {
        this(PriceCurrency.getInstance(in.readString()),
                (BigDecimal) in.readSerializable(),
                (BigDecimal) in.readSerializable(),
                (ExchangeRate) in.readSerializable(),
                in.readInt() > 0,
                restoreCurrencyToPriceMapFromParcel(in),
                restoreCurrencyToPriceMapFromParcel(in));
    }

    private ImmutableNetPriceImpl(@NonNull PriceCurrency currency,
                                  @NonNull BigDecimal totalPrice,
                                  @NonNull BigDecimal possiblyIncorrectTotalPrice,
                                  @NonNull ExchangeRate exchangeRate,
                                  boolean areAllExchangeRatesValid,
                                  @NonNull Map<PriceCurrency, BigDecimal> currencyToPriceMap,
                                  @NonNull Map<PriceCurrency, BigDecimal> notExchangedPrices) {
        this.currency = Preconditions.checkNotNull(currency);
        this.totalPrice = Preconditions.checkNotNull(totalPrice);
        this.possiblyIncorrectTotalPrice = Preconditions.checkNotNull(possiblyIncorrectTotalPrice);
        this.exchangeRate = Preconditions.checkNotNull(exchangeRate);
        this.areAllExchangeRatesValid = areAllExchangeRatesValid;
        this.currencyToPriceMap = Preconditions.checkNotNull(currencyToPriceMap);
        this.notExchangedPriceMap = Preconditions.checkNotNull(notExchangedPrices);

        // Note: The actual model utils stuff is somewhat slow due to the NumberFormats behind the scenes. We pre-cache here
        this.decimalFormattedPrice = calculateDecimalFormattedPrice();
        this.currencyFormattedPrice = calculateCurrencyFormattedPrice();
        this.currencyCodeFormattedPrice = calculateCurrencyCodeFormattedPrice();
    }

    @NonNull
    private static Map<PriceCurrency, BigDecimal> restoreCurrencyToPriceMapFromParcel(@NonNull Parcel in) {
        final Map<PriceCurrency, BigDecimal> currencyToPriceMap = new HashMap<>();
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
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
        return decimalFormattedPrice;
    }

    @NonNull
    @Override
    public String getCurrencyFormattedPrice() {
        return this.currencyFormattedPrice;
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        return currencyCodeFormattedPrice;
    }

    @NonNull
    private String getCurrencyCodeFormattedStringFromMap(Map<PriceCurrency, BigDecimal> map) {
        final List<String> currencyStrings = new ArrayList<>();
        for (PriceCurrency currency : map.keySet()) {
            currencyStrings.add(ModelUtils.getCurrencyCodeFormattedValue(map.get(currency), currency));
        }
        return TextUtils.join("; ", currencyStrings);
    }

    @NonNull
    @Override
    public PriceCurrency getCurrency() {
        return currency;
    }

    @NonNull
    @Override
    public String getCurrencyCode() {
        if (notExchangedPriceMap.size() > 1) {
            final List<String> currencyStrings = new ArrayList<>();
            for (PriceCurrency currency : notExchangedPriceMap.keySet()) {
                currencyStrings.add(currency.getCurrencyCode());
            }
            return TextUtils.join("; ", currencyStrings);
        } else {
            return currency.getCurrencyCode();
        }
    }

    @Override
    public int getCurrencyCodeCount() {
        return notExchangedPriceMap.size();
    }

    @NonNull
    @Override
    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    @Override
    public String toString() {
        return getCurrencyFormattedPrice();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Map<PriceCurrency, BigDecimal> getImmutableOriginalPrices() {
        return Collections.unmodifiableMap(notExchangedPriceMap);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currency.getCurrencyCode());
        dest.writeSerializable(totalPrice);
        dest.writeSerializable(possiblyIncorrectTotalPrice);
        dest.writeSerializable(exchangeRate);
        dest.writeInt(areAllExchangeRatesValid ? 1 : 0);

        // Finally, write maps
        writeMapToParcel(dest, currencyToPriceMap);
        writeMapToParcel(dest, notExchangedPriceMap);
    }

    @NonNull
    private String calculateDecimalFormattedPrice() {
        if (areAllExchangeRatesValid) {
            return ModelUtils.getDecimalFormattedValue(totalPrice);
        } else {
            return getCurrencyCodeFormattedStringFromMap(currencyToPriceMap);
        }
    }

    @NonNull
    private String calculateCurrencyFormattedPrice() {
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
    private String calculateCurrencyCodeFormattedPrice() {
            return getCurrencyCodeFormattedStringFromMap(notExchangedPriceMap);
    }

    private void writeMapToParcel(@NonNull Parcel dest, @NonNull Map<PriceCurrency, BigDecimal> map) {
        dest.writeInt(map.size());
        for (final Map.Entry<PriceCurrency, BigDecimal> entry : map.entrySet()) {
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
