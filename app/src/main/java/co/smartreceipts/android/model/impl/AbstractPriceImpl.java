package co.smartreceipts.android.model.impl;

import co.smartreceipts.android.model.Price;

/**
 * Provides common methods that all {@link Price} implementations use
 */
public abstract class AbstractPriceImpl implements Price {

    protected static final float EPSILON = 1f / (Price.ROUNDING_PRECISION + 2f);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPriceImpl)) return false;

        Price that = (Price) o;

        if (!getCurrency().equals(that.getCurrency())) {
            return false;
        }
        if (Math.abs(getPriceAsFloat() - that.getPriceAsFloat()) > EPSILON) {
            return false;
        }
        return getCurrencyFormattedPrice().equals(that.getCurrencyFormattedPrice());
    }

    @Override
    public int hashCode() {
        int result = getPrice().hashCode();
        result = 31 * result + getCurrency().hashCode();
        result = 31 * result + getCurrencyFormattedPrice().hashCode();
        return result;
    }
}
