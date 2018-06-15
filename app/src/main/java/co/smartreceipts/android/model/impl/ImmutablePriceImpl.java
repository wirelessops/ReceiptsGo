package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;

/**
 * Defines an immutable implementation of the {@link co.smartreceipts.android.model.Price} interface
 */
public final class ImmutablePriceImpl extends AbstractPriceImpl {

    private static final int ROUNDING_PRECISION = Price.ROUNDING_PRECISION + 2;

    private final BigDecimal price;
    private final PriceCurrency currency;
    private final ExchangeRate exchangeRate;
    private final int decimalPrecision;
    private final String decimalFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyCodeFormattedPrice; // Note: We create/cache this as it's common, slower operation

    public ImmutablePriceImpl(@NonNull BigDecimal price, @NonNull PriceCurrency currency, @NonNull ExchangeRate exchangeRate) {
        this(price, currency, exchangeRate, Price.DEFAULT_DECIMAL_PRECISION);
    }

    public ImmutablePriceImpl(@NonNull BigDecimal price, @NonNull PriceCurrency currency, @NonNull ExchangeRate exchangeRate,
                              int decimalPrecision) {
        this.price = price.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.decimalPrecision = decimalPrecision;

        // Note: The actual model utils stuff is somewhat slow due to the NumberFormats behind the scenes. We pre-cache here
        this.decimalFormattedPrice = ModelUtils.getDecimalFormattedValue(price, decimalPrecision);
        this.currencyFormattedPrice = ModelUtils.getCurrencyFormattedValue(price, currency, decimalPrecision);
        this.currencyCodeFormattedPrice = ModelUtils.getCurrencyCodeFormattedValue(price, currency, decimalPrecision);
    }

    private ImmutablePriceImpl(@NonNull Parcel in) {
        this.price = (BigDecimal) in.readSerializable();
        this.currency = PriceCurrency.getInstance(in.readString());
        this.exchangeRate = (ExchangeRate) in.readSerializable();
        this.decimalPrecision = in.readInt();

        // Note: The actual model utils stuff is somewhat slow due to the NumberFormats behind the scenes. We pre-cache here
        this.decimalFormattedPrice = ModelUtils.getDecimalFormattedValue(price, decimalPrecision);
        this.currencyFormattedPrice = ModelUtils.getCurrencyFormattedValue(price, currency, decimalPrecision);
        this.currencyCodeFormattedPrice = ModelUtils.getCurrencyCodeFormattedValue(price, currency, decimalPrecision);
    }

    @Override
    public float getPriceAsFloat() {
        return price.floatValue();
    }

    @Override
    @NonNull
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    @NonNull
    public String getDecimalFormattedPrice() {
        return decimalFormattedPrice;
    }

    @Override
    @NonNull
    public String getCurrencyFormattedPrice() {
        return currencyFormattedPrice;
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        return currencyCodeFormattedPrice;
    }

    @Override
    @NonNull
    public PriceCurrency getCurrency() {
        return currency;
    }

    @Override
    @NonNull
    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    @Override
    public int getCurrencyCodeCount() {
        return 1;
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
        dest.writeSerializable(price);
        dest.writeString(getCurrencyCode());
        dest.writeSerializable(exchangeRate);
        dest.writeInt(decimalPrecision);
    }

    public static final Creator<ImmutablePriceImpl> CREATOR = new Creator<ImmutablePriceImpl>() {
        public ImmutablePriceImpl createFromParcel(Parcel source) {
            return new ImmutablePriceImpl(source);
        }

        public ImmutablePriceImpl[] newArray(int size) {
            return new ImmutablePriceImpl[size];
        }
    };

}
