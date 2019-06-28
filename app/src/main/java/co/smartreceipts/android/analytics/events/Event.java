package co.smartreceipts.android.analytics.events;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * A simple model-class contract, which can be used to assist with tracking usage analytics in the app
 */
public interface Event {

    /**
     * Defines the category in which this event resides
     */
    interface Category {

        /**
         * @return the {@link String} representation of this
         */
        @NonNull
        String name();
    }

    /**
     * Defines the name of this specific event
     */
    interface Name {

        /**
         * @return the {@link String} representation of this
         */
        @NonNull
        String name();
    }

    /**
     * @return the {@link Category} associated with this event
     */
    @NonNull
    Category category();

    /**
     * @return the {@link Name} associated with this event
     */
    @NonNull
    Name name();

    /**
     * @return a {@link List} of {@link DataPoint} objeccts associated with this event
     */
    @NonNull
    List<DataPoint> getDataPoints();

}
