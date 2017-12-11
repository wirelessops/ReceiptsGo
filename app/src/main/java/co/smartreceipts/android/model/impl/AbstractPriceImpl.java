package co.smartreceipts.android.model.impl;

import co.smartreceipts.android.model.Price;

/**
 * Provides common methods that all {@link co.smartreceipts.android.model.Price} implementations use
 */
abstract class AbstractPriceImpl implements Price {

    protected static final float EPSILON = 1f / (Price.ROUNDING_PRECISION + 2f);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof AbstractPriceImpl)) return false;

        Price that = (Price) o;

        if (!getCurrency().equals(that.getCurrency())) {
            return false;
        }
        if (Math.abs(getPrice().floatValue() - that.getPrice().floatValue()) > EPSILON) {
            return false;
        }
        if (!getCurrencyFormattedPrice().equals(that.getCurrencyFormattedPrice())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getPrice().hashCode();
        result = 31 * result + getCurrency().hashCode();
        result = 31 * result + getCurrencyFormattedPrice().hashCode();
        return result;
    }
}
