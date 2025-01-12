package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.model.Receipt;

/**
 * A filter implementation of {@link AndFilter} for {@link Receipt}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class ReceiptAndFilter extends AndFilter<Receipt>{

	public ReceiptAndFilter() {
		super();
	}
	
	public ReceiptAndFilter(List<Filter<Receipt>> filters) {
		super(filters);
	}
	
	protected ReceiptAndFilter(JSONObject json) throws JSONException {
		super(json);
	}
	
	
	@Override
	Filter<Receipt> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getReceiptFilter(json);
	}

}
