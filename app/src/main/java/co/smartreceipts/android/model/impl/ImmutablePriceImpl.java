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

    public ImmutablePriceImpl(@NonNull BigDecimal price, @NonNull PriceCurrency currency, @NonNull ExchangeRate exchangeRate) {
        this(price, currency, exchangeRate, Price.DEFAULT_DECIMAL_PRECISION);
    }

    public ImmutablePriceImpl(@NonNull BigDecimal price, @NonNull PriceCurrency currency, @NonNull ExchangeRate exchangeRate,
                              int decimalPrecision) {
        this.price = price.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.decimalPrecision = decimalPrecision;
    }

    private ImmutablePriceImpl(@NonNull Parcel in) {
        this.price = (BigDecimal) in.readSerializable();
        this.currency = PriceCurrency.getInstance(in.readString());
        this.exchangeRate = (ExchangeRate) in.readSerializable();
        this.decimalPrecision = in.readInt();
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
        return ModelUtils.getDecimalFormattedValue(price, decimalPrecision);
    }

    @Override
    @NonNull
    public String getCurrencyFormattedPrice() {
        return ModelUtils.getCurrencyFormattedValue(price, currency, decimalPrecision);
    }

    @NonNull
    @Override
    public String getCurrencyCodeFormattedPrice() {
        return ModelUtils.getCurrencyCodeFormattedValue(price, currency, decimalPrecision);
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
