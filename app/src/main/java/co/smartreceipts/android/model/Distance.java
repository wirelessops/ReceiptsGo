package co.smartreceipts.android.model;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.sync.model.Syncable;

public interface Distance extends Parcelable, Priceable, Comparable<Distance>, Syncable {

    String PARCEL_KEY = Distance.class.getName();
    int RATE_PRECISION = 3;

    /**
     * Gets the primary key id for this distance
     *
     * @return the distance's autoincrement id
     */
    int getId();

    /**
     * Gets the parent trip for this distance. This should never be {@code null}.
     *
     * @return - the parent {@link Trip}
     */
    @NonNull
    Trip getTrip();

    /**
     * Gets the location to which this distance occurred (e.g. drove to Atlanta)
     *
     * @return the location as a {@link String}
     */
    @NonNull
    String getLocation();

    /**
     * Gets the decimal representation of the distance travelled
     *
     * @return - a {@link BigDecimal} containing the distance travelled
     */
    BigDecimal getDistance();

    /**
     * A "decimal-formatted" distance, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2123144444"
     *
     * @return the decimal formatted distance {@link String}
     */
    String getDecimalFormattedDistance();

    /**
     * Returns the date on which this distance occurred
     *
     * @return the {@link Date} this distance occurred
     */
    Date getDate();

    /**
     * Gets a formatted version of the date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param context   - the current {@link Context}
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for this distance
     */
    String getFormattedDate(Context context, String separator);

    /**
     * Gets the time zone in which the date was set
     *
     * @return - the {@link TimeZone} for the date
     */
    TimeZone getTimeZone();

    /**
     * The rate for which this distance may be reimbursed
     *
     * @return a {@link BigDecimal} representation of the reimbursement rate
     */
    BigDecimal getRate();

    /**
     * A "decimal-formatted" rate, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2"
     *
     * @return the decimal formatted rate {@link String}
     */
    String getDecimalFormattedRate();

    /**
     * The "currency-formatted" rate, which would appear as "$25.20" or "$25,20" as determined by the user's locale
     *
     * @return - the currency formatted rate {@link String}
     */
    String getCurrencyFormattedRate();

    /**
     * Gets the user defined comment for this receipt
     *
     * @return - the current comment as a {@link String}
     */
    @NonNull
    String getComment();

}
