package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Receipt;

/**
 * A filter implementation that tests if the category of a {@link Receipt} as determined
 * by {@link Receipt#getCategory()} is equal to a predefined value
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class ReceiptCategoryFilter implements Filter<Receipt> {

	private static final String CATEGORY = "category";
	
	private final String mCategory;
	
	/**
	 * Default constructor for this {@link Filter} that takes in a specific category {@link String}
	 * that our {@link Receipt#getCategory()} must equal for the {@link #accept(Receipt)} to
	 * return true
	 * 
	 * @param category - the category to check
	 */
	public ReceiptCategoryFilter(final String category) {
		if (category == null) {
			throw new IllegalArgumentException("ReceiptCategoryFilter requires a non-null category");
		}
		mCategory = category;
	}
	
	/**
	 * A package-private constructor that enables us to recreate this filter via a
	 * {@link JSONObject} representation
	 * 
	 * @param json - the {@link JSONObject} representation of this filter
	 * @throws JSONException - throw if our provide {@link JSONObject} is invalid
	 */
	ReceiptCategoryFilter(JSONObject json) throws JSONException {
		mCategory = json.getString(CATEGORY);
	}
	
	@Override
	public boolean accept(Receipt t) {
		return mCategory.equals(t.getCategory().getName());
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(CATEGORY, mCategory);
		return json;
	}

	@Override
	public List<Filter<Receipt>> getChildren() {
		return null;
	}

	@Override
	public int getNameResource() {
		return R.string.filter_name_receipt_category;
	}

	@Override
	public FilterType getType() {
		return FilterType.String;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mCategory == null) ? 0 : mCategory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		ReceiptCategoryFilter other = (ReceiptCategoryFilter) obj;
		if (mCategory == null) {
			if (other.mCategory != null) {
				return false;
			}
		} 
		else if (!mCategory.equals(other.mCategory)) {
			return false;
		}
		return true;
	}

}
