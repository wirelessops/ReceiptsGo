package com.wops.receiptsgo.model.impl;

import android.os.Parcel;

import androidx.annotation.NonNull;

import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.wops.receiptsgo.model.gson.ExchangeRate;

/**
 * Defines an immutable implementation of the {@link com.wops.receiptsgo.model.Price} interface
 */
public final class SinglePriceImpl extends AbstractPriceImpl {

    private final Money money;
    private final ExchangeRate exchangeRate;
    private final String decimalFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyCodeFormattedPrice; // Note: We create/cache this as it's common, slower operation


    public SinglePriceImpl(@NonNull BigDecimal price, @NonNull CurrencyUnit currency, @NonNull ExchangeRate exchangeRate) {
        this.money = Money.of(currency, price, RoundingMode.HALF_EVEN);
        this.exchangeRate = exchangeRate;

        // Pre-cache formatted values here
        this.decimalFormattedPrice = Companion.getMoneyFormatter().print(money);
        this.currencyFormattedPrice = money.getCurrencyUnit().getSymbol().concat(decimalFormattedPrice);
        this.currencyCodeFormattedPrice = money.getCurrencyUnit().getCode().concat(" ").concat(decimalFormattedPrice);
    }

    private SinglePriceImpl(@NonNull Parcel in) {
        this.money = (Money) in.readSerializable();
        this.exchangeRate = (ExchangeRate) in.readSerializable();

        // Pre-cache formatted values here
        this.decimalFormattedPrice = Companion.getMoneyFormatter().print(money);
        this.currencyFormattedPrice = money.getCurrencyUnit().getSymbol().concat(decimalFormattedPrice);
        this.currencyCodeFormattedPrice = money.getCurrencyUnit().getCode().concat(" ").concat(decimalFormattedPrice);
    }

    @Override
    public float getPriceAsFloat() {
        return money.getAmount().floatValue();
    }

    @Override
    @NonNull
    public BigDecimal getPrice() {
        return money.getAmount();
    }

    @NonNull
    @Override
    public BigMoney getMoney() {
        return money.toBigMoney();
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
    public CurrencyUnit getCurrency() {
        return money.getCurrencyUnit();
    }

    @Override
    @NonNull
    public String getCurrencyCode() {
        return money.getCurrencyUnit().getCode();
    }

    @Override
    public boolean isSingleCurrency() {
        return true;
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
        dest.writeSerializable(money);
        dest.writeSerializable(exchangeRate);
    }

    public static final Creator<SinglePriceImpl> CREATOR = new Creator<SinglePriceImpl>() {
        public SinglePriceImpl createFromParcel(Parcel source) {
            return new SinglePriceImpl(source);
        }

        public SinglePriceImpl[] newArray(int size) {
            return new SinglePriceImpl[size];
        }
    };
}
