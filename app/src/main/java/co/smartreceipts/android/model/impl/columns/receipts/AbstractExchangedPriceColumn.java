package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.ImmutableNetPriceImpl;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Allows us to genericize how different prices are converted to a trip's base currency
 */
public abstract class AbstractExchangedPriceColumn extends AbstractColumnImpl<Receipt> {

    private final Context mContext;

    public AbstractExchangedPriceColumn(int id, @NonNull String name, @NonNull SyncState syncState, @NonNull Context context) {
        super(id, name, syncState);
        mContext = context;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        final Price price = getPrice(receipt);
        final ExchangeRate exchangeRate = price.getExchangeRate();
        final PriceCurrency baseCurrency = receipt.getTrip().getTripCurrency();
        if (exchangeRate.supportsExchangeRateFor(baseCurrency)) {
            return ModelUtils.getDecimalFormattedValue(price.getPrice().multiply(exchangeRate.getExchangeRate(baseCurrency)));
        } else {
            return mContext.getString(R.string.undefined);
        }
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<Receipt> rows) {
        if (!rows.isEmpty()) {
            final PriceBuilderFactory factory = new PriceBuilderFactory();
            final List<Price> prices = new ArrayList<>(rows.size());
            for (final Receipt receipt : rows) {
                factory.setCurrency(receipt.getTrip().getTripCurrency());
                prices.add(getPrice(receipt));
            }
            factory.setPrices(prices, rows.get(0).getTrip().getTripCurrency());
            final Price netPrice = factory.build();
            if (netPrice instanceof ImmutableNetPriceImpl) {
                // TODO: This should be slightly less hacky
                if (((ImmutableNetPriceImpl) netPrice).areAllExchangeRatesValid()) {
                    return netPrice.getDecimalFormattedPrice();
                } else {
                    return netPrice.getCurrencyCodeFormattedPrice();
                }
            } else {
                return netPrice.getDecimalFormattedPrice();
            }
        } else {
            return "";
        }
    }

    @NonNull
    protected abstract Price getPrice(@NonNull Receipt receipt);
}
