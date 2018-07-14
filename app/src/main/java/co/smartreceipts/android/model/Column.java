package co.smartreceipts.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

import co.smartreceipts.android.sync.model.Syncable;

/**
 * Provides a contract for how each individual column in a report should operate
 */
public interface Column<T> extends Syncable, Draggable<Column<T>> {

    int UNKNOWN_ID = -1;

    /**
     * Gets the unique identifier number for this column
     *
     * @return the unique id or {@link #UNKNOWN_ID} if none is defined
     */
    int getId();

    /**
     * Gets the column type of this particular column
     *
     * @return int enum type from {@link ColumnDefinitions}
     */
    int getType();

    /**
     * Gets the column header resource id of this particular column
     *
     * @return the {@link StringRes} of the header for this particular column
     */
    @StringRes
    int getHeaderStringResId();

    /**
     * Gets the value of a particular row item as determined by this column. If this column
     * represented the name of this item, {@link T}, then this would return the name.
     *
     * @param rowItem the row item to get the value for (based on the column definition)
     * @return the {@link String} representation of the value //TODO: Make this non-null
     */
    @Nullable
    String getValue(@NonNull T rowItem);

    /**
     * Gets the footer value for this particular column based on a series of rows. The
     * footer in a report generally tends to correspond to some type of summation.
     *
     * @param rows the {@link List} of rows of {@link T} to process for the footer
     * @return the {@link String} representation of the footer for this particular column
     */
    @NonNull
    String getFooter(@NonNull List<T> rows);
}
