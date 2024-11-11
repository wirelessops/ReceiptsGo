package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Receipt;

public class ReceiptMaximumPriceFilter implements Filter<Receipt> {

	private final static String MAX_PRICE = "maxprice";
	private final static String CURRENCY_CODE = "currencycode";

	private final float mMaxPrice;
	private final String mCurrencyCode;

	public ReceiptMaximumPriceFilter(float maxPrice, String currencyCode) {
		if (currencyCode == null)
			throw new IllegalArgumentException(
					"ReceiptMinPriceFilter requires non-null currencyCode");

		mMaxPrice = maxPrice;
		mCurrencyCode = currencyCode;
	}

	public ReceiptMaximumPriceFilter(JSONObject json) throws JSONException {
		this.mMaxPrice = (float) json.getDouble(MAX_PRICE);
		this.mCurrencyCode = json.getString(CURRENCY_CODE);
	}

	@Override
	public boolean accept(Receipt t) {
		return t.getPrice().getPriceAsFloat() <= mMaxPrice
				&& t.getPrice().getCurrencyCode().equalsIgnoreCase(mCurrencyCode);
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(MAX_PRICE, mMaxPrice);
		json.put(CURRENCY_CODE, mCurrencyCode);
		return json;
	}

	@Override
	public List<Filter<Receipt>> getChildren() {
		return null;
	}

	@Override
	public int getNameResource() {
		return R.string.filter_name_receipt_max_price;
	}

	@Override
	public FilterType getType() {
		return FilterType.Float;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mCurrencyCode == null) ? 0 : mCurrencyCode.hashCode());
		result = prime * result + Float.floatToIntBits(mMaxPrice);
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

		ReceiptMaximumPriceFilter other = (ReceiptMaximumPriceFilter) obj;

		if (mCurrencyCode == null) {
			if (other.mCurrencyCode != null)
				return false;
		} else if (!mCurrencyCode.equals(other.mCurrencyCode))
			return false;

		if (Float.floatToIntBits(mMaxPrice) != Float
				.floatToIntBits(other.mMaxPrice))
			return false;

		return true;
	}

}
