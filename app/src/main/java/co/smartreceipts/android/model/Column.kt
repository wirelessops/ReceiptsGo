package co.smartreceipts.android.model

import androidx.annotation.StringRes
import co.smartreceipts.android.sync.model.Syncable

/**
 * Provides a contract for how each individual column in a report should operate
 */
interface Column<T> : Keyed, Syncable, Draggable<Column<T>> {

    /**
     * Gets the column type of this particular column
     *
     * @return int enum type from [ColumnDefinitions]
     */
    val type: Int

    /**
     * Gets the column header resource id of this particular column
     *
     * @return the [StringRes] of the header for this particular column
     */
    @get:StringRes
    val headerStringResId: Int

    /**
     * Gets the value of a particular row item as determined by this column. If this column
     * represented the name of this item, [T], then this would return the name.
     *
     * @param rowItem the row item to get the value for (based on the column definition)
     * @return the [String] representation of the value
     */
    fun getValue(rowItem: T): String

    /**
     * Gets the footer value for this particular column based on a series of rows. The
     * footer in a report generally tends to correspond to some type of summation.
     *
     * @param rows the [List] of rows of [T] to process for the footer
     * @return the [String] representation of the footer for this particular column
     */
    fun getFooter(rows: List<T>): String
}
