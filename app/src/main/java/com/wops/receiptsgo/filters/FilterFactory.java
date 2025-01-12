package com.wops.receiptsgo.filters;

import android.view.LayoutInflater;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;

/**
 * Factory that allows us to rebuild our previous filter list from a {@link JSONObject}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 *
 */
public class FilterFactory {
	
	static final String CLASS_NAME = "class";

	private FilterFactory() { }
	
	public static final View getReceiptFiltersView(Filter<Receipt> filter, LayoutInflater inflater) {
		if (filter instanceof ReceiptAndFilter) {
			final ReceiptAndFilter receiptAndFilter = (ReceiptAndFilter) filter;
		}
		else if (filter instanceof ReceiptOrFilter) {
			final ReceiptOrFilter receiptOrFilter = (ReceiptOrFilter) filter;
		}
		else if (filter instanceof ReceiptNotFilter) {
			final ReceiptNotFilter receiptNotFilter = (ReceiptNotFilter) filter;
		}
		return null;
	}
	
	
	private static final View getReceiptFiltersView(Filter<Receipt> filter, LayoutInflater inflater, String prefix,
			String suffix) {

		return null;
	}

	/**
	 * Builds a {@link Filter} for a {@link Receipt} based on a {@link JSONObject}
	 * 
	 * @param json - the {@link JSONObject} that represents this filter
	 * @return a {@link Filter} of {@link Receipt}
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	public static final Filter<Receipt> getReceiptFilter(JSONObject json) throws JSONException {
		final String className = json.getString(CLASS_NAME);

		if (ReceiptOrFilter.class.getName().equals(className)) {
			return new ReceiptOrFilter(json);
		} 
		else if (ReceiptAndFilter.class.getName().equals(className)) {
			return new ReceiptAndFilter(json);
		} 
		else if (ReceiptNotFilter.class.getName().equals(className)) {
			return new ReceiptNotFilter(json);
		} 
		else if (ReceiptIsReimbursableFilter.class.getName().equals(className)) {
			return new ReceiptIsReimbursableFilter(json);
		} 
		else if (ReceiptSelectedFilter.class.getName().equals(className)) {
			return new ReceiptSelectedFilter(json);
		} 
		else if (ReceiptCategoryFilter.class.getName().equals(className)) {
			return new ReceiptCategoryFilter(json);
		} 
		else if (ReceiptMaximumPriceFilter.class.getName().equals(className)) {
			return new ReceiptMaximumPriceFilter(json);
		} 
		else if (ReceiptMinimumPriceFilter.class.getName().equals(className)) {
			return new ReceiptMinimumPriceFilter(json);
		} 
		else if (ReceiptOnOrAfterDayFilter.class.getName().equals(className)) {
			return new ReceiptOnOrAfterDayFilter(json);
		} 
		else if (ReceiptOnOrBeforeDayFilter.class.getName().equals(className)) {
			return new ReceiptOnOrBeforeDayFilter(json);
		} 
		else {
			return null;
		}
	}
	
	/**
	 * Builds a {@link Filter} for a {@link Trip} based on a {@link JSONObject}
	 * 
	 * @param json - the {@link JSONObject} that represents this filter
	 * @return a {@link Filter} of {@link Trip}
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	public static final Filter<Trip> getTripFilter(JSONObject json) throws JSONException {
		final String className = json.getString(CLASS_NAME);
		
		if(TripAndFilter.class.getName().equals(className)){
			return new TripAndFilter(json); 
		} 
		else if(TripEndsOnOrAfterDayFilter.class.getName().equals(className)){
			return new TripEndsOnOrAfterDayFilter(json); 
		} 
		else if(TripEndsOnOrBeforeDayFilter.class.getName().equals(className)){
			return new TripEndsOnOrBeforeDayFilter(json); 
		} 
		else if(TripMaximumPriceFilter.class.getName().equals(className)){
			return new TripMaximumPriceFilter(json); 
		} 
		else if(TripMinimumPriceFilter.class.getName().equals(className)){
			return new TripMinimumPriceFilter(json); 
		} 
		else if(TripNotFilter.class.getName().equals(className)){
			return new TripNotFilter(json); 
		} 
		else if(TripOrFilter.class.getName().equals(className)){
			return new TripOrFilter(json); 
		} 
		else if(TripStartsOnOrAfterDayFilter.class.getName().equals(className)){
			return new TripStartsOnOrAfterDayFilter(json); 
		} 
		else if(TripStartsOnOrBeforeDayFilter.class.getName().equals(className)){
			return new TripStartsOnOrBeforeDayFilter(json); 
		} 
		else {
			return null;
		}
	}
}
