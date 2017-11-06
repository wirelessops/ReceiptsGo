package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
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
@Deprecated
public final class ImmutableLegacyNetPriceImpl extends AbstractPriceImpl {

    private final PriceCurrency currency;
    private final BigDecimal possiblyIncorrectTotalPrice;
    private final ExchangeRate exchangeRate;
    private final Map<PriceCurrency, BigDecimal> currencyToPriceMap;

    public ImmutableLegacyNetPriceImpl(@NonNull List<Price> prices) {
        this.currencyToPriceMap = new HashMap<>();
        BigDecimal possiblyIncorrectTotalPrice = new BigDecimal(0);
        PriceCurrency currency = null;
        for (final Price price : prices) {
            possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.add(price.getPrice());
            final BigDecimal priceToAdd = currencyToPriceMap.containsKey(price.getCurrency()) ? currencyToPriceMap.get(price.getCurrency()).add(price.getPrice()) : price.getPrice();
            currencyToPriceMap.put(price.getCurrency(), priceToAdd);
            if (currency == null) {
                currency = price.getCurrency();
            } else if (!currency.equals(price.getCurrency())) {
                currency = PriceCurrency.MIXED_CURRENCY; // Mark as fixed if multiple
            }
        }
        this.currency = currency;
        this.possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice;
        final ExchangeRateBuilderFactory builder = new ExchangeRateBuilderFactory();
        if (this.currency != null) {
            builder.setBaseCurrency(this.currency);
        }
        this.exchangeRate = builder.build();
    }

    @SuppressWarnings("unchecked")
    private ImmutableLegacyNetPriceImpl(@NonNull Parcel in) {
        this(PriceCurrency.getInstance(in.readString()),
                (BigDecimal) in.readSerializable(),
                (ExchangeRate) in.readSerializable(),
                restoreCurrencyToPriceMapFromParcel(in));
    }

    private ImmutableLegacyNetPriceImpl(@NonNull PriceCurrency currency,
                                        @NonNull BigDecimal possiblyIncorrectTotalPrice,
                                        @NonNull ExchangeRate exchangeRate,
                                        @NonNull Map<PriceCurrency, BigDecimal> currencyToPriceMap) {
        this.currency = Preconditions.checkNotNull(currency);
        this.possiblyIncorrectTotalPrice = Preconditions.checkNotNull(possiblyIncorrectTotalPrice);
        this.exchangeRate = Preconditions.checkNotNull(exchangeRate);
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
        return possiblyIncorrectTotalPrice.floatValue();
    }

    @NonNull
    @Override
    public BigDecimal getPrice() {
        return possiblyIncorrectTotalPrice;
    }

    @NonNull
    @Override
    public String getDecimalFormattedPrice() {
        return ModelUtils.getDecimalFormattedValue(possiblyIncorrectTotalPrice);
    }

    @NonNull
    @Override
    public String getCurrencyFormattedPrice() {
        final List<String> currencyStrings = new ArrayList<>();
        for (PriceCurrency currency : currencyToPriceMap.keySet()) {
            currencyStrings.add(ModelUtils.getCurrencyFormattedValue(currencyToPriceMap.get(currency), currency));
        }
        return TextUtils.join("; ", currencyStrings);
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        final List<String> currencyStrings = new ArrayList<>();
        for (PriceCurrency currency : currencyToPriceMap.keySet()) {
            currencyStrings.add(ModelUtils.getCurrencyCodeFormattedValue(currencyToPriceMap.get(currency), currency));
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
        return currency.getCurrencyCode();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currency.getCurrencyCode());
        dest.writeSerializable(possiblyIncorrectTotalPrice);
        dest.writeSerializable(exchangeRate);

        // Finally, write the map
        dest.writeInt(currencyToPriceMap.size());
        for(final Map.Entry<PriceCurrency, BigDecimal> entry : currencyToPriceMap.entrySet()){
            dest.writeString(entry.getKey().getCurrencyCode());
            dest.writeSerializable(entry.getValue());
        }
    }


    public static final Creator<ImmutableLegacyNetPriceImpl> CREATOR = new Creator<ImmutableLegacyNetPriceImpl>() {
        public ImmutableLegacyNetPriceImpl createFromParcel(Parcel source) {
            return new ImmutableLegacyNetPriceImpl(source);
        }

        public ImmutableLegacyNetPriceImpl[] newArray(int size) {
            return new ImmutableLegacyNetPriceImpl[size];
        }
    };
}
