package com.wops.receiptsgo.filters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.wops.receiptsgo.R;

/**
 * A filter implementation that combines multiple other {@link Filter}
 * implementations in the manner of a logical AND operation.
 * 
 * @author Will Baumann
 * @since July 12, 2014
 * 
 */
public abstract class AndFilter<T> implements Filter<T> {

	private static final String AND_FILTERS = "and_filters";

	private final CopyOnWriteArrayList<Filter<T>> mFilters;

	/**
	 * Additional logical AND calls may be added to this composite
	 * {@link Filter} via the {@link #and(Filter)} method.
	 */
	public AndFilter() {
		mFilters = new CopyOnWriteArrayList<>();
	}

	/**
	 * A preset list of logical AND filters may be added to this constructor so
	 * that chaining via the {@link #and(Filter)} method is not required
	 * 
	 * @param filters
	 *            - the {@link List} of {@link Filter} to add
	 */
	public AndFilter(List<Filter<T>> filters) {
		mFilters = new CopyOnWriteArrayList<>(filters);
	}


	/**
	 * A package-private constructor that enables us to recreate this filter via
	 * a {@link JSONObject} representation
	 * 
	 * @param json
	 *            - the {@link JSONObject} representation of this filter
	 * @throws JSONException
	 *             - throw if our provide {@link JSONObject} is invalid
	 */
	protected AndFilter(JSONObject json) throws JSONException {
		final List<Filter<T>> filters = new ArrayList<>();
		final JSONArray filtersArray = json.getJSONArray(AND_FILTERS);
		for (int i = 0; i < filtersArray.length(); i++) {
			filters.add(getFilter(filtersArray.getJSONObject(i)));
		}
		mFilters = new CopyOnWriteArrayList<>(filters);
	}

	/**
	 * Retrieves a {@link Filter} implementation from a given JSON object. This
	 * is required in order to properly reconstruct our filters from JSON
	 * 
	 * @param json
	 *            - the {@link JSONObject} representing a particular filter
	 * @return a {@link Filter} implementation
	 * @throws JSONException
	 *             - throw if our provide {@link JSONObject} is invalid
	 */
	abstract Filter<T> getFilter(JSONObject json) throws JSONException;

	/**
	 * Adds another filter for the logical AND comparison that will be performed
	 * via the {@link #accept(Object)} method is called.
	 * 
	 * @param filter
	 *            - the {@link Filter} to add
	 * @return this instance of {@link AndFilter} for method chaining
	 */
	public AndFilter<T> and(final Filter<T> filter) {
		mFilters.add(filter);
		return this;
	}

	@Override
	public boolean accept(T t) {
		// iterating through all filters
		// and return false if one of the filter is not accepted
		for (final Filter<T> filter : mFilters) {
			if (!filter.accept(t)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public JSONObject getJsonRepresentation() throws JSONException {
		final JSONArray filtersArray = new JSONArray();
		for (final Filter<T> filter : mFilters) {
			filtersArray.put(filter.getJsonRepresentation());
		}
		final JSONObject json = new JSONObject();
		json.put(FilterFactory.CLASS_NAME, this.getClass().getName());
		json.put(AND_FILTERS, filtersArray);
		return json;
	}

	@Override
	public List<Filter<T>> getChildren() {
		return new ArrayList<>(mFilters);
	}

	@Override
	public int getNameResource() {
		return R.string.filter_name_and;
	}

	@Override
	public FilterType getType() {
		return FilterType.Composite;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mFilters == null) ? 0 : mFilters.hashCode());
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
		AndFilter<?> other = (AndFilter<?>) obj;
		if (mFilters == null) {
			if (other.mFilters != null)
				return false;
		} else if (!mFilters.equals(other.mFilters))
			return false;
		return true;
	}

}
