package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Trip;

public class TripMinimumPriceFilter implements Filter<Trip>{

	private final static String MIN_PRICE = "minprice";
	private final static String CURRENCY_CODE = "currencycode";

	private final float mMinPrice;
	private final String mCurrencyCode;

	public TripMinimumPriceFilter(float minPrice, String currencyCode) {
		if (currencyCode == null)
			throw new IllegalArgumentException(
					"ReceiptMinPriceFilter requires non-null currencyCode");

		mMinPrice = minPrice;
		mCurrencyCode = currencyCode;
	}

	public TripMinimumPriceFilter(JSONObject json) throws JSONException {
		this.mMinPrice = (float) json.getDouble(MIN_PRICE);
		this.mCurrencyCode = json.getString(CURRENCY_CODE);
	}

	@Override
	public boolean accept(Trip t) {
		return t.getPrice().getPriceAsFloat() >= mMinPrice
				&& t.getPrice().getCurrencyCode().equalsIgnoreCase(mCurrencyCode);
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(MIN_PRICE, mMinPrice);
		json.put(CURRENCY_CODE, mCurrencyCode);
		return json;
	}

	@Override
	public List<Filter<Trip>> getChildren() {
		return null;
	}

	@Override
	public int getNameResource() {
		return R.string.filter_name_trip_min_price;
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
				+ ((mCurrencyCode == null) ? 0 : mCurrencyCode.hashCode())
				+ (int) (mMinPrice * 100);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		TripMinimumPriceFilter other = (TripMinimumPriceFilter) obj;
		return (mMinPrice == other.mMinPrice 
				&& mCurrencyCode.equals(other.mCurrencyCode));
	}

}
