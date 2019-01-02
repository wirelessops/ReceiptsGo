package co.smartreceipts.android.date

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.StringRes
import co.smartreceipts.android.R
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.log.Logger
import co.smartreceipts.android.utils.rx.RxSchedulers
import io.reactivex.Scheduler
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named


/**
 * A standard Date Formatter instance, which we can use to ensure that all dates within the app are
 * properly formatted based on the timezone, user's locale, and user's default settings in the app.
 */
@ApplicationScope
class DateFormatter @Inject constructor(private val context: Context,
                                        private val userPreferenceManager: UserPreferenceManager,
                                        @Named(RxSchedulers.IO) private val scheduler: Scheduler) {

    @Suppress("EnumEntryName")
    enum class DateFormatOption(@StringRes val stringResId: Int) {
        /**
         * The default format option for Android, which is [java.text.DateFormat.SHORT]. This
         * formats dates according to the phone's default set of preferences.
         */
        Default(R.string.pref_general_date_format_default_entryValue),

        /**
         * The default American style of date, which appears like "MM/DD/YYYY"
         *
         * When using this, we would expect:
         *  - "December 26, 2018" to appear as "12/26/2018"
         *  - "January 3, 2019" to appear as "1/3/2019"
         */
        M_d_yyyy(R.string.pref_general_date_format_M_d_yyyy_entryValue),

        /**
         * The default European style of date, which appears like "DD/MM/YYYY"
         *
         * When using this, we would expect:
         *  - "December 26, 2018" to appear as "26/12/2018"
         *  - "January 3, 2019" to appear as "3/1/2019"
         */
        d_M_yyyy(R.string.pref_general_date_format_d_M_yyyy_entryValue),

        /**
         * A date format, which appears like "YYYY/MM/DD"
         *
         * When using this, we would expect:
         *  - "December 26, 2018" to appear as "2018/12/26"
         *  - "January 3, 2019" to appear as "2019/01/03"
         */
        yyyy_MM_dd(R.string.pref_general_date_format_yyyy_MM_dd_entryValue);

    }

    private val threadLocalCache = ConcurrentHashMap<TimeZone, DateFormatThreadLocal>()

    private var separator = DateUtils.getDateSeparator(context)
    private var dateFormatOption = DateFormatOption.Default

    /**
     * Initializes our date formatter, so that we properly apply all of the desired user settings
     */
    @SuppressLint("CheckResult")
    fun initialize() {
        // Pre-fetch our initial date format and separator value to avoid reading this on the UI thread
        userPreferenceManager.getSingle(UserPreference.General.DateSeparator)
                .subscribeOn(scheduler)
                .subscribe { separator ->
                    this.separator = separator
                }
        userPreferenceManager.getSingle(UserPreference.General.DateFormat)
                .map { getDateFormatOption(it) }
                .subscribeOn(scheduler)
                .subscribe { dateFormatOption ->
                    this.dateFormatOption = dateFormatOption
                }

        // Monitor for future changes to our date separator
        userPreferenceManager.userPreferenceChangeStream
                .subscribeOn(scheduler)
                .filter { it == UserPreference.General.DateSeparator }
                .map { userPreferenceManager[UserPreference.General.DateSeparator] }
                .doOnNext {
                    // Whenever we get a result, clear our cache
                    threadLocalCache.clear()
                    Logger.info(this, "The user changed the date separator value to {}.", it)
                }
                .subscribe {
                    separator = it
                }

        // Monitor for future changes to our date format
        userPreferenceManager.userPreferenceChangeStream
                .subscribeOn(scheduler)
                .filter { it == UserPreference.General.DateFormat }
                .map { userPreferenceManager[UserPreference.General.DateFormat] }
                .map { getDateFormatOption(it) }
                .doOnNext {
                    // Whenever we get a result, clear our cache
                    threadLocalCache.clear()
                    Logger.info(this, "The user changed the date format value to {}.", it)
                }
                .subscribe {
                    dateFormatOption = it
                }
    }

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
        // Next, leverage a ThreadLocal to fetch a thread safe handle to our date format
        val dateFormatThreadLocal = threadLocalCache.getOrPut(timeZone) {
            return@getOrPut DateFormatThreadLocal(getDateFormat(dateFormatOption, timeZone))
        }
        val dateFormat = dateFormatThreadLocal.get()
        return dateFormat.format(date)
    }

    /**
     * Gets a formatted version of a date based for a particular timezone for a specific formatting
     * option. Please note this variant provides no caching or performance optimizations and should
     * only be used when we require the ability to present a specific [DateFormatOption] that is
     * NOT the user's preferred variant
     *
     * @param displayableDate the [DisplayableDate] to format
     * @param dateFormatOption the [DateFormatOption] to use
     *
     * @return the formatted date string for the provided date
     */
    fun getFormattedDate(displayableDate: DisplayableDate, dateFormatOption: DateFormatOption): String {
        return getDateFormat(dateFormatOption, displayableDate.timeZone).format(displayableDate.date)
    }

    /**
     * Fetches one of our [DateFormatOption] values from a provider user preference [String]
     *
     * @param formatPreference the user preference as a [String]
     *
     * @return the corresponding [DateFormatOption] or [DateFormatOption.Default] if it cannot be found
     */
    private fun getDateFormatOption(formatPreference: String) : DateFormatOption {
        DateFormatOption.values().forEach {
            if (context.getString(it.stringResId) == formatPreference) {
                return it
            }
        }
        return DateFormatOption.Default
    }

    /**
     * Gets the [DateFormat] for a particular [DateFormatOption] and [TimeZone]
     *
     * @param dateFormatOption the [DateFormatOption] to build this format from
     * @param timeZone the [TimeZone] to use for this format
     *
     * @return the resultant [DateFormat] to use
     */
    private fun getDateFormat(dateFormatOption: DateFormatOption, timeZone: TimeZone) : DateFormat {
        val formatString = when (dateFormatOption) {
            DateFormatOption.M_d_yyyy -> "M${separator}d${separator}YYYY"
            DateFormatOption.d_M_yyyy -> "d${separator}M${separator}YYYY"
            DateFormatOption.yyyy_MM_dd -> "YYYY${separator}MM${separator}dd"
            DateFormatOption.Default -> {
                // For the default option, we get the system default and replace it's separator with the user preference
                val systemDefaultDateFormat = android.text.format.DateFormat.getDateFormat(context) as SimpleDateFormat
                val defaultDateSeparator = DateUtils.getDateSeparator(context)
                systemDefaultDateFormat.toPattern().replace(defaultDateSeparator, separator)
            }
        }
        val format = SimpleDateFormat(formatString, Locale.getDefault())
        format.timeZone = timeZone // Hack to shift the timezone appropriately
        return format
    }

}