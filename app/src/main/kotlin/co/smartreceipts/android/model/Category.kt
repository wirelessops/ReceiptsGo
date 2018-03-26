package co.smartreceipts.android.model

import android.os.Parcelable

import co.smartreceipts.android.sync.model.Syncable

/**
 * A contract definition by which we can track receipt categories
 */
interface Category : Parcelable, Syncable, Draggable<Category> {

    /**
     * The database primary key id for this category
     */
    val id: Int

    /**
     * The full-name representation of this category
     */
    val name: String

    /**
     * The "code" associated with this category
     */
    val code: String
}
