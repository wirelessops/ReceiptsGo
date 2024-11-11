package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Trip;

public class TripMaximumPriceFilter implements Filter<Trip>{

	private final static String MIN_PRICE = "minprice";
	private final static String CURRENCY_CODE = "currencycode";

	private final float mMaxPrice;
	private final String mCurrencyCode;

	public TripMaximumPriceFilter(float maxPrice, String currencyCode) {
		if (currencyCode == null)
			throw new IllegalArgumentException(
					"ReceiptMinPriceFilter requires non-null currencyCode");

		mMaxPrice = maxPrice;
		mCurrencyCode = currencyCode;
	}

	public TripMaximumPriceFilter(JSONObject json) throws JSONException {
		this.mMaxPrice = (float) json.getDouble(MIN_PRICE);
		this.mCurrencyCode = json.getString(CURRENCY_CODE);
	}

	@Override
	public boolean accept(Trip t) {
		return t.getPrice().getPriceAsFloat() <= mMaxPrice
				&& t.getPrice().getCurrencyCode().equalsIgnoreCase(mCurrencyCode);
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(MIN_PRICE, mMaxPrice);
		json.put(CURRENCY_CODE, mCurrencyCode);
		return json;
	}

	@Override
	public List<Filter<Trip>> getChildren() {
		return null;
	}

	@Override
	public int getNameResource() {
		return R.string.filter_name_trip_max_price;
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
				+ (int) (mMaxPrice * 100);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		TripMaximumPriceFilter other = (TripMaximumPriceFilter) obj;
		return (mMaxPrice == other.mMaxPrice 
				&& mCurrencyCode.equals(other.mCurrencyCode));
	}

}
