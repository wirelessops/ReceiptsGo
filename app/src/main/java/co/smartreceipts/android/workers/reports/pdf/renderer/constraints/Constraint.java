package co.smartreceipts.android.workers.reports.pdf.renderer.constraints;

import androidx.annotation.NonNull;

public interface Constraint<T> {

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
