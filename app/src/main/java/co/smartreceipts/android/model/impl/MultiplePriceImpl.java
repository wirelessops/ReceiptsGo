package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.format.MoneyFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;

/**
 * Defines an immutable implementation of the {@link co.smartreceipts.android.model.Price} interface
 * for a collection of other price objects.
 */
public final class MultiplePriceImpl extends AbstractPriceImpl {

    /**
     * Total money value with currency scale
     */
    private final BigMoney totalMoney;

    private final BigMoney possiblyIncorrectTotalPrice;
    private final boolean areAllExchangeRatesValid;

    private final ExchangeRate exchangeRate;

    private final Map<CurrencyUnit, BigMoney> currencyToPriceMap;

    private final Map<CurrencyUnit, BigMoney> notExchangedPriceMap;

    private final String decimalFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyFormattedPrice; // Note: We create/cache this as it's common, slower operation
    private final String currencyCodeFormattedPrice; // Note: We create/cache this as it's common, slower operation

    public MultiplePriceImpl(@NonNull CurrencyUnit baseCurrency, @NonNull List<Price> prices) {

        notExchangedPriceMap = new HashMap<>();
        currencyToPriceMap = new HashMap<>();

        BigMoney total = BigMoney.zero(baseCurrency);

        BigMoney possiblyIncorrectTotalPrice = BigMoney.zero(baseCurrency);
        boolean areAllExchangeRatesValid = true;

        for (final Price price : prices) {

            notExchangedPriceMap.put(price.getCurrency(),
                    notExchangedPriceMap.containsKey(price.getCurrency()) ?
                            notExchangedPriceMap.get(price.getCurrency()).plus(price.getMoney()) :
                            price.getMoney());

            final BigMoney moneyToAdd;
            final CurrencyUnit currencyForPriceToAdd;
            if (price.getExchangeRate().supportsExchangeRateFor(baseCurrency)) {

                final BigDecimal exchangeRate = price.getExchangeRate().getExchangeRate(baseCurrency.getCode());

                moneyToAdd = price.getCurrency().equals(baseCurrency) ? price.getMoney() : price.getMoney().convertedTo(baseCurrency, exchangeRate);
                currencyForPriceToAdd = baseCurrency;

                total = total.plus(moneyToAdd);

            } else {
                // If not, let's just hope for the best with whatever we have to add
                moneyToAdd = price.getMoney();
                currencyForPriceToAdd = price.getCurrency();

                areAllExchangeRatesValid = false;
            }

            possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.plus(moneyToAdd.getAmount());

            final BigMoney priceForCurrency = currencyToPriceMap.containsKey(currencyForPriceToAdd) ?
                    currencyToPriceMap.get(currencyForPriceToAdd).plus(moneyToAdd) : moneyToAdd;
            currencyToPriceMap.put(currencyForPriceToAdd, priceForCurrency);
        }

        this.totalMoney = total.withCurrencyScale(RoundingMode.HALF_EVEN);
        this.possiblyIncorrectTotalPrice = possiblyIncorrectTotalPrice.withCurrencyScale(RoundingMode.HALF_EVEN);
        this.areAllExchangeRatesValid = areAllExchangeRatesValid;
        this.exchangeRate = new ExchangeRateBuilderFactory().setBaseCurrency(baseCurrency).build();

        // Pre-cache formatted values here
        MoneyFormatter formatter = Companion.getMoneyFormatter();
        this.decimalFormattedPrice = calculateDecimalFormattedPrice(formatter);
        this.currencyFormattedPrice = calculateCurrencyFormattedPrice(formatter);
        this.currencyCodeFormattedPrice = calculateCurrencyCodeFormattedPrice(formatter);
    }

    @SuppressWarnings("unchecked")
    private MultiplePriceImpl(@NonNull Parcel in) {
        this((BigMoney) in.readSerializable(),
                (BigMoney) in.readSerializable(),
                (ExchangeRate) in.readSerializable(),
                in.readInt() > 0,
                restoreCurrencyToPriceMapFromParcel(in),
                restoreCurrencyToPriceMapFromParcel(in));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(totalMoney);
        dest.writeSerializable(possiblyIncorrectTotalPrice);
        dest.writeSerializable(exchangeRate);
        dest.writeInt(areAllExchangeRatesValid ? 1 : 0);

        // Finally, write maps
        writeMapToParcel(dest, currencyToPriceMap);
        writeMapToParcel(dest, notExchangedPriceMap);
    }

    private MultiplePriceImpl(@NonNull BigMoney totalMoney,
                              @NonNull BigMoney possiblyIncorrectTotalPrice,
                              @NonNull ExchangeRate exchangeRate,
                              boolean areAllExchangeRatesValid,
                              @NonNull Map<CurrencyUnit, BigMoney> currencyToPriceMap,
                              @NonNull Map<CurrencyUnit, BigMoney> notExchangedPrices) {
        this.totalMoney = Preconditions.checkNotNull(totalMoney);
        this.possiblyIncorrectTotalPrice = Preconditions.checkNotNull(possiblyIncorrectTotalPrice);
        this.exchangeRate = Preconditions.checkNotNull(exchangeRate);
        this.areAllExchangeRatesValid = areAllExchangeRatesValid;
        this.currencyToPriceMap = Preconditions.checkNotNull(currencyToPriceMap);
        this.notExchangedPriceMap = Preconditions.checkNotNull(notExchangedPrices);

        // Note: The actual model utils stuff is somewhat slow due to the NumberFormats behind the scenes. We pre-cache here
        MoneyFormatter formatter = Companion.getMoneyFormatter();
        this.decimalFormattedPrice = calculateDecimalFormattedPrice(formatter);
        this.currencyFormattedPrice = calculateCurrencyFormattedPrice(formatter);
        this.currencyCodeFormattedPrice = calculateCurrencyCodeFormattedPrice(formatter);
    }

    @Override
    public float getPriceAsFloat() {
        if (areAllExchangeRatesValid) {
            return totalMoney.getAmount().floatValue();
        } else {
            return possiblyIncorrectTotalPrice.getAmount().floatValue();
        }
    }

    @NonNull
    @Override
    public BigDecimal getPrice() {
        if (areAllExchangeRatesValid) {
            return totalMoney.getAmount();
        } else {
            return possiblyIncorrectTotalPrice.getAmount();
        }
    }

    @NonNull
    @Override
    public BigMoney getMoney() {
        if (areAllExchangeRatesValid) {
            return totalMoney;
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
    private String getCurrencyCodeFormattedStringFromMap(Map<CurrencyUnit, BigMoney> map, MoneyFormatter formatter) {
        final List<String> currencyStrings = new ArrayList<>();
        for (CurrencyUnit currency : map.keySet()) {
            final BigMoney money = map.get(currency).withCurrencyScale(RoundingMode.HALF_EVEN);

            String decimalValue = formatter.print(money);
            String codeFormatted = money.getCurrencyUnit().getCode().concat(" ").concat(decimalValue);

            currencyStrings.add(codeFormatted);
        }
        return TextUtils.join("; ", currencyStrings);
    }

    @NonNull
    @Override
    public CurrencyUnit getCurrency() {
        return totalMoney.getCurrencyUnit();
    }

    @NonNull
    @Override
    public String getCurrencyCode() {
        if (notExchangedPriceMap.size() > 1) {
            final List<String> currencyStrings = new ArrayList<>();
            for (CurrencyUnit currency : notExchangedPriceMap.keySet()) {
                currencyStrings.add(currency.getCode());
            }
            return TextUtils.join("; ", currencyStrings);
        } else {
            return totalMoney.getCurrencyUnit().getCode();
        }
    }

    @Override
    public boolean isSingleCurrency() {
        return notExchangedPriceMap.size() == 1;
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

    public Map<CurrencyUnit, BigMoney> getImmutableOriginalPrices() {
        return Collections.unmodifiableMap(notExchangedPriceMap);
    }

    @NonNull
    private String calculateDecimalFormattedPrice(MoneyFormatter formatter) {
        if (areAllExchangeRatesValid) {
            return formatter.print(totalMoney);
        } else {
            return getCurrencyCodeFormattedStringFromMap(currencyToPriceMap, formatter);
        }
    }

    @NonNull
    private String calculateCurrencyFormattedPrice(MoneyFormatter formatter) {
        if (areAllExchangeRatesValid) {
            return totalMoney.getCurrencyUnit().getSymbol()
                    .concat(formatter.print(totalMoney));
        } else {
            final List<String> currencyStrings = new ArrayList<>();
            for (CurrencyUnit currency : currencyToPriceMap.keySet()) {
                final BigMoney money = currencyToPriceMap.get(currency).withCurrencyScale(RoundingMode.HALF_EVEN);
                currencyStrings.add(currency.getSymbol().concat(formatter.print(money)));
            }
            return TextUtils.join("; ", currencyStrings);
        }
    }

    @NonNull
    private String calculateCurrencyCodeFormattedPrice(MoneyFormatter formatter) {
        return getCurrencyCodeFormattedStringFromMap(notExchangedPriceMap, formatter);
    }

    private void writeMapToParcel(@NonNull Parcel dest, @NonNull Map<CurrencyUnit, BigMoney> map) {
        dest.writeInt(map.size());
        for (final Map.Entry<CurrencyUnit, BigMoney> entry : map.entrySet()) {
            dest.writeString(entry.getKey().getCode());
            dest.writeSerializable(entry.getValue());
        }
    }

    @NonNull
    private static Map<CurrencyUnit, BigMoney> restoreCurrencyToPriceMapFromParcel(@NonNull Parcel in) {
        final Map<CurrencyUnit, BigMoney> currencyToPriceMap = new HashMap<>();
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final CurrencyUnit currency = CurrencyUnit.of(in.readString());
            final BigMoney price = (BigMoney) in.readSerializable();
            currencyToPriceMap.put(currency, price);
        }
        return currencyToPriceMap;
    }

    public static final Creator<MultiplePriceImpl> CREATOR = new Creator<MultiplePriceImpl>() {
        public MultiplePriceImpl createFromParcel(Parcel source) {
            return new MultiplePriceImpl(source);
        }

        public MultiplePriceImpl[] newArray(int size) {
            return new MultiplePriceImpl[size];
        }
    };
}
