package com.wops.receiptsgo.model;

import android.os.Parcelable;

/**
 * Tracks the current processing status the a given {@link Trip} or
 * {@link Receipt} may be in (e.g. what is just created, has it been
 * submitted, was it approved, etc.)
 */
public interface ProcessingStatus extends Parcelable {

    /**
     * Returns the current processing status string representation for this item (e.g. has it been submitted)
     *
     * @return - the current processing status {@link String}
     */
    String getProcessingStatus();

}
