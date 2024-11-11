package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Receipt;

public class ReceiptIsReimbursableFilter implements Filter<Receipt> {

	public ReceiptIsReimbursableFilter() {
		// empty
	}

	public ReceiptIsReimbursableFilter(JSONObject json) throws JSONException {
		// empty
	}

	@Override
	public boolean accept(Receipt t) {
		return t.isReimbursable();
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		return json;
	}

	@Override
	public List<Filter<Receipt>> getChildren() {
		return null;
	}

	@Override
	public int getNameResource() {
		return R.string.filter_name_receipt_reimbursable;
	}

	@Override
	public FilterType getType() {
		return FilterType.Boolean;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getClass().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		// only compare by class since this filter has no parameters
		return (obj != null && getClass() == obj.getClass());
	}
}
