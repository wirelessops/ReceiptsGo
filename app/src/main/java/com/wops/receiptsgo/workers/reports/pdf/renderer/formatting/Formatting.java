package com.wops.receiptsgo.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;

public interface Formatting<T> {

    /**
     * @return the value of this particular constraint
     */
    @NonNull
    T value();

    /**
     * @return the type of this particular constraint
     */
    @NonNull
    Class<T> getType();

}
