package com.wops.receiptsgo.filters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.wops.receiptsgo.model.Trip;

/**
 * A filter implementation of {@link OrFilter} for {@link Trip}
 * 
 * @author Will Baumann
 * @since July 08, 2014
 * 
 */
public class TripOrFilter extends OrFilter<Trip>{

	public TripOrFilter() {
		super();
	}
	
	public TripOrFilter(List<Filter<Trip>> filters) {
		super(filters);
	}
	
	protected TripOrFilter(JSONObject json) throws JSONException {
		super(json);
	}
	
	
	@Override
	Filter<Trip> getFilter(JSONObject json) throws JSONException {
		return FilterFactory.getTripFilter(json);
	}

}
