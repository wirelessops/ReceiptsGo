package com.wops.receiptsgo.filters;

import android.R.string;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Interface to enable filtering of a particular data type
 * 
 * @author Will Baumann
 * @since July 08, 2014
 *
 */
public interface Filter<T> {
	
	/**
	 * Apply a filter operation in order to determine if we should accept this particular object as part
	 * of our output total.
	 * @param t - the object of type {@link T} to check
	 * @return {@link true} if it should be accepted, {@link false} otherwise
	 */
    boolean accept(T t);

    /**
     *
     * @return a {@link JSONObject} that represents this particular filter. This is used to enable us to
     * reconstruct filters if persistence is desired.
     * @throws JSONException if invalid parameters were presentFirstTimeInformation
     */
    JSONObject getJsonRepresentation() throws JSONException;


	/**
	 * @return a complete {@link List} of {@link Filter} objects that are considered as children to this {@link Filter}
	 *         or {@code null} if it does not contain any children
	 */
    List<Filter<T>> getChildren();


	/**
	 * @return an Android {@link string} resource for this filter's display name
	 */
    int getNameResource();


	/**
	 * @return the {@link FilterType} of this particular filter
	 */
    FilterType getType();

}
