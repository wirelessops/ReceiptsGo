package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.model.Receipt;

/**
 * A filter implementation of {@link OrFilter} for {@link Receipt}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class ReceiptOrFilter extends OrFilter<Receipt>{

	public ReceiptOrFilter() {
		super();
	}
	
	public ReceiptOrFilter(List<Filter<Receipt>> filters) {
		super(filters);
	}
	
	protected ReceiptOrFilter(JSONObject json) throws JSONException {
		super(json);
	}
	
	
	@Override
	Filter<Receipt> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getReceiptFilter(json);
	}

}
