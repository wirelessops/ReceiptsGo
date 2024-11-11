package com.wops.receiptsgo.model

import android.os.Parcelable
import com.wops.receiptsgo.adapters.DistanceListItem
import com.wops.receiptsgo.date.DisplayableDate
import com.wops.receiptsgo.model.utils.ModelUtils
import co.smartreceipts.core.sync.model.SyncState
import co.smartreceipts.core.sync.model.Syncable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.sql.Date
import java.util.*

/**
 * An immutable [Distance] implementation to track distance.
 */
@Parcelize
class Distance(
    override val id: Int,
    override val uuid: UUID,
    override val price: Price,
    override val syncState: SyncState,
    /**
     * The parent [Trip] for this distance
     */
    val trip: Trip,
    /**
     * The [String] location to which this distance occurred (e.g. drove to Atlanta)
     */
    val location: String,
    /**
     * The [BigDecimal] representation of the distance travelled
     */
    val distance: BigDecimal,
    /**
     * The [BigDecimal] rate for which this distance may be reimbursed
     */
    val rate: BigDecimal,
    /**
     * The [DisplayableDate] on which this distance occurred
     */
    val displayableDate: DisplayableDate,
    /**
     * The user defined comment [String] for this receipt
     */
    val comment: String,
    /**
     * The payment method associated with this receipt item.
     */
    val paymentMethod: PaymentMethod,
    val autoCompleteMetadata: AutoCompleteMetadata
) : Keyed, Parcelable, Priceable, Comparable<Distance>, Syncable, DistanceListItem {

    /**
     * The [Date] in which the [displayableDate] was set
     */
    val date: Date get() = displayableDate.date

    /**
     * The [TimeZone] in which the [displayableDate] was set
     */
    val timeZone: TimeZone get() = displayableDate.timeZone

    /**
     * A "decimal-formatted" distance [String], which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2123144444"
     */
    val decimalFormattedDistance: String get() = ModelUtils.getDecimalFormattedValue(distance, DISTANCE_PRECISION)

    /**
     * A "decimal-formatted" rate [String], which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2"
     */
    val decimalFormattedRate: String get() = ModelUtils.getDecimalFormattedValue(rate, RATE_PRECISION)

    /**
     * The "currency-formatted" rate [String], which would appear as "$25.20" or "$25,20" as determined by the user's locale
     */
    val currencyFormattedRate: String
        get() {
            val precision = if (decimalFormattedRate.endsWith("0")) Price.TOTAL_DECIMAL_PRECISION else RATE_PRECISION
            return ModelUtils.getCurrencyFormattedValue(rate, price.currency, precision)
        }


    override fun toString(): String {
        return "Distance [uuid=$uuid, location=$location, distance=$distance, displayableDate=$displayableDate, rate=$rate, price= $price, " +
                "comment=$comment, paymentMethod=$paymentMethod, autoCompleteMetadata=$autoCompleteMetadata]"
    }

    override fun compareTo(other: Distance): Int {
        return other.displayableDate.date.compareTo(displayableDate.date)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Distance

        if (id != other.id) return false
        if (uuid != other.uuid) return false
        if (trip != other.trip) return false
        if (location != other.location) return false
        if (distance != other.distance) return false
        if (rate != other.rate) return false
        if (price != other.price) return false
        if (displayableDate != other.displayableDate) return false
        if (comment != other.comment) return false
        if (paymentMethod != other.paymentMethod) return false
        if (autoCompleteMetadata != other.autoCompleteMetadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + uuid.hashCode()
        result = 31 * result + trip.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + distance.hashCode()
        result = 31 * result + rate.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + displayableDate.hashCode()
        result = 31 * result + comment.hashCode()
        result = 31 * result + paymentMethod.hashCode()
        result = 31 * result + autoCompleteMetadata.hashCode()
        return result
    }

    companion object {
        @JvmField val PARCEL_KEY: String = Distance::class.java.name
        const val RATE_PRECISION = 3
        const val DISTANCE_PRECISION = 2
    }

}
