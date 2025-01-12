package com.wops.receiptsgo.model

import android.os.Parcelable
import com.wops.receiptsgo.date.DisplayableDate
import com.wops.receiptsgo.search.Searchable
import com.wops.receiptsgo.utils.StrictModeConfiguration
import com.wops.core.sync.model.SyncState
import com.wops.core.sync.model.Syncable
import kotlinx.parcelize.Parcelize
import java.io.File
import java.sql.Date
import java.util.*

/**
 * A mostly immutable implementation of the [Receipt] interface that
 * serves as the default implementation.
 */
@Parcelize
class Receipt constructor(
    override val id: Int,
    override val uuid: UUID,
    /**
     * The "index" of this receipt relative to others. If this was the second earliest receipt, it would appear
     * as a receipt of index 2.
     */
    val index: Int,
    /**
     * The parent trip for this receipt. This can only be null if it's detached from a [Trip]
     * (e.g. if it's a converted distance).
     */
    val trip: Trip,
    /**
     * The file attached to this receipt or `null` if none is presentFirstTimeInformation
     */
    val file: File?,
    /**
     * The payment method associated with this receipt item.
     */
    val paymentMethod: PaymentMethod,
    /**
     * The name of this receipt. This should never be `null`.
     */
    val name: String,
    /**
     * The [Category] to which this receipt is attached
     */
    val category: Category,
    /**
     * The user defined comment for this receipt
     */
    val comment: String,
    override val price: Price,
    /**
     * The [Price] tax associated with this receipt
     */
    val tax: Price,
    /**
     * The [Price] second (optional) tax associated with this receipt
     */
    val tax2: Price,
    /**
     * The [DisplayableDate] during which this receipt occurred
     */
    val displayableDate: DisplayableDate,
    /**
     * Checks if the receipt was marked as Reimbursable (i.e. counting towards the total) or not
     */
    val isReimbursable: Boolean,
    /**
     * Checks if this receipt should be printed as a full page in the PDF report
     */
    val isFullPage: Boolean,
    /**
     *  Checks if this receipt is currently selected or not
     */
    val isSelected: Boolean,
    /**
     * An extra [String], which certain white-label builds might have
     */
    val extraEditText1: String?,
    /**
     * An extra [String], which certain white-label builds might have
     */
    val extraEditText2: String?,
    /**
     * An extra [String], which certain white-label builds might have
     */
    val extraEditText3: String?,
    override val syncState: SyncState,
    override val customOrderId: Long,
    val autoCompleteMetadata: AutoCompleteMetadata
) : Keyed, Parcelable, Priceable, Draggable<Receipt>, Syncable, Searchable {

    /**
     * The [Date] in which the [displayableDate] was set
     */
    val date: Date get() = displayableDate.date

    /**
     * The [TimeZone] in which the [displayableDate] was set
     */
    val timeZone: TimeZone get() = displayableDate.timeZone

    /**
     * The name of this Receipt's file from [.getFile].
     */
    val fileName: String
        get() = file?.name ?: ""

    /**
     * The last updated time or `-1` if we don't have a file
     *
     * Java uses immutable [File], so when we rename our files as part of a receipt update, we might rename it
     * to the same file name as the old receipt. By tracking the last update time as well, we can determine if this file
     * was updated between two "like" receipts
     */
    val fileLastModifiedTime: Long
        get() = StrictModeConfiguration.permitDiskReads { file?.lastModified() } ?: -1

    /**
     * The absolute path of this Receipt's file from [.getFile].
     */
    val filePath: String
        get() = file?.absolutePath ?: ""

    /**
     *  Checks if this receipt is connected to an image file
     */
    fun hasImage(): Boolean {
        return file?.name?.run {
            endsWith(".jpg", ignoreCase = true) || endsWith(".jpeg", ignoreCase = true) || endsWith(".png", ignoreCase = true)
        } ?: false
    }

    /**
     *  Checks if this receipt is connected to an PDF file
     */
    fun hasPDF(): Boolean {
        return file?.name?.endsWith(".pdf", ignoreCase = true) ?: false
    }

    fun hasExtraEditText1(): Boolean = extraEditText1 != null

    fun hasExtraEditText2(): Boolean = extraEditText2 != null

    fun hasExtraEditText3(): Boolean = extraEditText3 != null

    override fun toString(): String {
        return "DefaultReceiptImpl{" +
                "id=" + id +
                ", uuid='" + uuid.toString() +
                ", name='" + name + '\''.toString() +
                ", trip=" + trip.name +
                ", paymentMethod=" + paymentMethod +
                ", index=" + index +
                ", comment='" + comment + '\''.toString() +
                ", category=" + category +
                ", price=" + price.currencyFormattedPrice +
                ", displayableDate=" + displayableDate +
                ", timeZone=" + timeZone.id +
                ", isReimbursable=" + isReimbursable +
                ", isFullPage=" + isFullPage +
                ", autoCompleteMetadata=" + autoCompleteMetadata +
                ", extraEditText1='" + extraEditText1 + '\''.toString() +
                ", extraEditText2='" + extraEditText2 + '\''.toString() +
                ", extraEditText3='" + extraEditText3 + '\''.toString() +
                ", isSelected=" + isSelected +
                ", file=" + file +
                ", customOrderId=" + customOrderId +
                '}'.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Receipt) return false

        val that = other as Receipt?

        if (id != that!!.id) return false
        if (uuid != that.uuid) return false
        if (isReimbursable != that.isReimbursable) return false
        if (isFullPage != that.isFullPage) return false
        if (trip != that.trip) return false
        if (paymentMethod != that.paymentMethod) return false
        if (index != that.index) return false
        if (name != that.name) return false
        if (comment != that.comment) return false
        if (category != that.category) return false
        if (price != that.price) return false
        if (tax != that.tax) return false
        if (tax2 != that.tax2) return false
        if (displayableDate != that.displayableDate) return false
        if (autoCompleteMetadata != that.autoCompleteMetadata) return false
        if (if (extraEditText1 != null) extraEditText1 != that.extraEditText1 else that.extraEditText1 != null)
            return false
        if (if (extraEditText2 != null) extraEditText2 != that.extraEditText2 else that.extraEditText2 != null)
            return false
        if (if (extraEditText3 != null) extraEditText3 != that.extraEditText3 else that.extraEditText3 != null)
            return false
        if (fileLastModifiedTime != that.fileLastModifiedTime) return false
        if (customOrderId != that.customOrderId) return false
        return if (file != null) file == that.file else that.file == null

    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + uuid.hashCode()
        result = 31 * result + trip.hashCode()
        result = 31 * result + paymentMethod.hashCode()
        result = 31 * result + index
        result = 31 * result + name.hashCode()
        result = 31 * result + comment.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + tax.hashCode()
        result = 31 * result + tax2.hashCode()
        result = 31 * result + displayableDate.hashCode()
        result = 31 * result + if (isReimbursable) 1 else 0
        result = 31 * result + if (isFullPage) 1 else 0
        result = 31 * result + autoCompleteMetadata.hashCode()
        result = 31 * result + (extraEditText1?.hashCode() ?: 0)
        result = 31 * result + (extraEditText2?.hashCode() ?: 0)
        result = 31 * result + (extraEditText3?.hashCode() ?: 0)
        result = 31 * result + if (file != null) file.hashCode() else 0
        result = 31 * result + fileLastModifiedTime.toInt()
        result = 31 * result + (customOrderId xor customOrderId.ushr(32)).toInt()
        return result
    }

    override fun compareTo(other: Receipt): Int {
        return if (customOrderId == other.customOrderId) {
            other.date.compareTo(date)
        } else {
            -java.lang.Long.compare(customOrderId, other.customOrderId)
        }
    }

    companion object {
        @JvmField
        val PARCEL_KEY: String = Receipt::class.java.name
    }

}