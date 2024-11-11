package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Receipt;

public final class LegacyReceiptMinimumPriceFilter implements Filter<Receipt> {

    private final static String MIN_PRICE = "minprice";

    private final float mMinPrice;

    public LegacyReceiptMinimumPriceFilter(float minPrice) {
        mMinPrice = minPrice;
    }

    public LegacyReceiptMinimumPriceFilter(JSONObject json) throws JSONException {
        this.mMinPrice = (float) json.getDouble(MIN_PRICE);

    }

    @Override
    public boolean accept(Receipt t) {
        return t.getPrice().getPriceAsFloat() >= mMinPrice;
    }

    @Override
    public JSONObject getJsonRepresentation() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
        json.put(MIN_PRICE, mMinPrice);
        return json;
    }

    @Override
    public List<Filter<Receipt>> getChildren() {
        return null;
    }

    @Override
    public int getNameResource() {
        return R.string.filter_name_receipt_min_price;
    }

    @Override
    public FilterType getType() {
        return FilterType.Float;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(mMinPrice);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        LegacyReceiptMinimumPriceFilter other = (LegacyReceiptMinimumPriceFilter) obj;


        if (Float.floatToIntBits(mMinPrice) != Float
                .floatToIntBits(other.mMinPrice))
            return false;

        return true;
    }
}
