package co.smartreceipts.android.date

import android.content.Context
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import java.sql.Date
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.HashMap


/**
 * A standard Date Formatter instance, which we can use to ensure that all dates within the app are
 * properly formatted based on the timezone, user's locale, and user's default settings in the app.
 */
@ApplicationScope
class DateFormatter @Inject constructor(private val context: Context,
                                        private val userPreferenceManager: UserPreferenceManager) {

    private val threadLocalCache = ConcurrentHashMap<DateFormatMetadata, DateFormatThreadLocal>()

    /**
     * Gets a formatted version of a date based for a particular timezone. We also apply the user's
     * in-app preferences to ensure this value appears in the desired format. For example, we might
     * expect the US Locale to return a result like "08/23/2014" by default for August 23rd, 2014,
     * if we set the separator as "/".
     *
     * @param displayableDate a [DisplayableDate]
     *
     * @return the formatted date string for the provided date
     */
    fun getFormattedDate(displayableDate: DisplayableDate): String {
        return getFormattedDate(displayableDate.date, displayableDate.timeZone)
    }

    /**
     * Gets a formatted version of a date based for a particular timezone. We also apply the user's
     * in-app preferences to ensure this value appears in the desired format. For example, we might
     * expect the US Locale to return a result like "08/23/2014" by default for August 23rd, 2014,
     * if we set the separator as "/".
     *
     * @param date the [java.util.Date] to format
     * @param timeZone the [TimeZone] to use for this date
     *
     * @return the formatted date string for the provided date
     */
    fun getFormattedDate(date: java.util.Date, timeZone: TimeZone): String {
        return getFormattedDate(Date(date.time), timeZone)
    }

    /**
     * Gets a formatted version of a date based for a particular timezone. We also apply the user's
     * in-app preferences to ensure this value appears in the desired format. For example, we might
     * expect the US Locale to return a result like "08/23/2014" by default for August 23rd, 2014,
     * if we set the separator as "/".
     *
     * @param date the [Date] to format
     * @param timeZone the [TimeZone] to use for this date
     *
     * @return the formatted date string for the provided date
     */
    fun getFormattedDate(date: Date, timeZone: TimeZone): String {
        // Build a set of metadata for this item to ensure that we can cache the results
        val dateFormatMetadata = DateFormatMetadata(timeZone)

        // Next, leverage a ThreadLocal to fetch a thread safe handle to our date format
        val dateFormatThreadLocal = threadLocalCache.getOrPut(dateFormatMetadata) {
            val format = android.text.format.DateFormat.getDateFormat(context)
            format.timeZone = timeZone // Hack to shift the timezone appropriately
            return@getOrPut DateFormatThreadLocal(format)
        }
        val dateFormat = dateFormatThreadLocal.get()
        val formattedDate = dateFormat.format(date)

        // Finally, replace our date separator instance with an appropriate one
        val separator = userPreferenceManager[UserPreference.General.DateSeparator]
        return formattedDate.replace(DateUtils.getDateSeparator(context), separator)
    }

    /**
     * A private data class, which we use to track metadata that could influence our date formats
     */
    private data class DateFormatMetadata(private val timeZone: TimeZone)

}