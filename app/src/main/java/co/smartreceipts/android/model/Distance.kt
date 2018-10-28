package co.smartreceipts.android.model

import android.content.Context
import android.os.Parcelable
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.sync.model.Syncable
import kotlinx.android.parcel.Parcelize
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
     * The [Date] on which this distance occurred
     */
    val date: Date,
    /**
     * The [TimeZone] in which the date was set
     */
    val timeZone: TimeZone,
    /**
     * The user defined comment [String] for this receipt
     */
    val comment: String
) : Keyed, Parcelable, Priceable, Comparable<Distance>, Syncable {

    /**
     * A "decimal-formatted" distance [String], which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2123144444"
     */
    val decimalFormattedDistance: String
        get() = ModelUtils.getDecimalFormattedValue(distance)

    /**
     * A "decimal-formatted" rate [String], which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2"
     */
    val decimalFormattedRate: String
        get() = ModelUtils.getDecimalFormattedValue(rate, RATE_PRECISION)

    /**
     * The "currency-formatted" rate [String], which would appear as "$25.20" or "$25,20" as determined by the user's locale
     */
    val currencyFormattedRate: String
        get() {
            val precision =
                if (decimalFormattedRate.endsWith("0")) Price.DEFAULT_DECIMAL_PRECISION else RATE_PRECISION
            return ModelUtils.getCurrencyFormattedValue(rate, price.currency, precision)
        }


    /**
     * Gets a formatted version of the date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param context   - the current [Context]
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for this distance
     */
    fun getFormattedDate(context: Context, separator: String): String =
        ModelUtils.getFormattedDate(date, timeZone, context, separator)

    override fun toString(): String {
        return "Distance [uuid=$uuid, mLocation=$location, mDistance=$distance, mDate=$date, mTimezone=$timeZone, mRate=$rate, mPrice= $price, mComment=$comment]"
    }

    override fun compareTo(other: Distance): Int {
        return other.date.compareTo(date)
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
        if (date != other.date) return false
        if (timeZone != other.timeZone) return false
        if (comment != other.comment) return false

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
        result = 31 * result + date.hashCode()
        result = 31 * result + timeZone.hashCode()
        result = 31 * result + comment.hashCode()
        return result
    }

    companion object {
        @JvmField val PARCEL_KEY: String = Distance::class.java.name
        const val RATE_PRECISION = 3
    }

}
