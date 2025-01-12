package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import com.wops.receiptsgo.model.Receipt;

/**
 * A filter implementation of {@link NotFilter} for {@link Receipt}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class ReceiptNotFilter extends NotFilter<Receipt> {

	public ReceiptNotFilter(Filter<Receipt> filter) {
		super(filter);
	}

	protected ReceiptNotFilter(JSONObject json) throws JSONException {
		super(json);
	}

	@Override
	Filter<Receipt> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getReceiptFilter(json);
	}

}
