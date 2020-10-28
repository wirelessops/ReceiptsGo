package co.smartreceipts.android.model.utils

import android.content.Context
import android.text.TextUtils
import android.text.format.DateFormat
import co.smartreceipts.android.date.DateUtils
import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Price.Companion.moneyFormatter
import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Date
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object ModelUtils {

    private const val EPSILON = 0.0001f

    val decimalFormat: DecimalFormat
        get() {
            // check for the case when Locale has changed
            if (_decimalFormat.decimalFormatSymbols != DecimalFormatSymbols()) {
                _decimalFormat = initDecimalFormat()
            }
            return _decimalFormat
        }

    val decimalSeparator: Char
        get() = decimalFormat.decimalFormatSymbols.decimalSeparator

    private var _decimalFormat: DecimalFormat = initDecimalFormat()

    private fun initDecimalFormat(): DecimalFormat {
        val format = DecimalFormat()
        format.isParseBigDecimal = true
        format.isGroupingUsed = true
        return format
    }


    @JvmStatic
    fun getFormattedDate(date: java.util.Date, timeZone: TimeZone, context: Context, separator: String): String =
        getFormattedDate(Date(date.time), timeZone, context, separator)


    /**
     * Gets a formatted version of a date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param date      - the [Date] to format
     * @param timeZone  - the [TimeZone] to use for this date
     * @param context   - the current [Context]
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for the start date
     */
    fun getFormattedDate(date: Date, timeZone: TimeZone, context: Context, separator: String): String {
        val format = DateFormat.getDateFormat(context)
        format.timeZone = timeZone // Hack to shift the timezone appropriately
        val formattedDate = format.format(date)
        return formattedDate.replace(DateUtils.getDateSeparator(context), separator)
    }

    /**
     * Generates "decimal-formatted" value, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2001910"
     * By default, this assumes a decimal precision of {@link PriceNew#TOTAL_DECIMAL_PRECISION}
     *
     * @param decimal - the [BigDecimal] to format
     * @param precision - the decimal places count
     * @return the decimal formatted price [String]
     */
    @JvmStatic
    @JvmOverloads
    fun getDecimalFormattedValue(decimal: BigDecimal, precision: Int = Price.TOTAL_DECIMAL_PRECISION): String {
        val money = BigMoney.of(CurrencyUtils.getDefaultCurrency(), decimal)

        return moneyFormatter.print(money.withScale(precision, RoundingMode.HALF_EVEN))
    }

    /**
     * The "currency-formatted" value, which would appear as "$25.20" or "$25,20" as determined by the user's locale.
     *
     * @param decimal          - the [BigDecimal] to format
     * @param currency         - the [CurrencyUnit] to use
     * @param precision - the desired decimal precision to use (eg 2 => "$25.20", 3 => "$25.200")
     * @return - the currency formatted price [String]
     */
    @JvmStatic
    fun getCurrencyFormattedValue(decimal: BigDecimal, currency: CurrencyUnit, precision: Int = -1): String {
        val money = BigMoney.of(currency, decimal)

        val decimalPlaces =
            when (precision) {
                -1 -> currency.decimalPlaces
                else -> precision
            }

        return money.currencyUnit.symbol + moneyFormatter.print(money.withScale(decimalPlaces, RoundingMode.HALF_EVEN))
    }

    /**
     * The "currency-code-formatted" value, which would appear as "USD 25.20" or "USD 25,20" as determined by the user's locale.
     *
     * @param decimal          - the [BigDecimal] to format
     * @param currency         - the [CurrencyUnit] to use. If this is {@code null}, return {@link #getDecimalFormattedValue(BigDecimal)}
     * @param precision - the desired decimal precision to use (eg 2 => "USD 25.20", 3 => "USD 25.200")
     * @return - the currency formatted price [String]
     */
    @JvmStatic
    fun getCurrencyCodeFormattedValue(decimal: BigDecimal, currency: CurrencyUnit, precision: Int = -1): String {
        val money = BigMoney.of(currency, decimal)

        val decimalPlaces =
            when (precision) {
                -1 -> currency.decimalPlaces
                else -> precision
            }

        return currency.code + " " + moneyFormatter.print(money.withScale(decimalPlaces, RoundingMode.HALF_EVEN))
    }


    /**
     * Tries to parse a string to find the underlying numerical value
     *
     * @param number the string containing a number (hopefully)
     * @return the [BigDecimal] value
     * @throws NumberFormatException if we cannot parse this number
     */
    @JvmStatic
    @Throws(NumberFormatException::class)
    fun parseOrThrow(number: String?): BigDecimal {
        if (number == null || TextUtils.isEmpty(number)) {
            throw NumberFormatException("Cannot parse an empty string")
        }

        // Note: for some locales grouping separator symbol is non-breaking space (code = 160),
        // but incoming string may contain general space => need to prepare such string before parsing
        val groupingSeparator = decimalFormat.decimalFormatSymbols.groupingSeparator.toString()
        val nonBreakingSpace = ("\u00A0").toString()

        val parsedNumber =
            when (groupingSeparator) {
                nonBreakingSpace -> decimalFormat.parse(number.replace(" ", nonBreakingSpace))
                else -> decimalFormat.parse(number)
            }

        return BigDecimal(parsedNumber.toString())
    }

    /**
     * Tries to parse a string to find the underlying numerical value
     *
     * @param number the string containing a number (hopefully)
     * @return the [BigDecimal] value or "0" if it cannot be found
     */
    @JvmStatic
    @JvmOverloads
    fun tryParse(number: String?, defaultValue: BigDecimal = BigDecimal.ZERO): BigDecimal {
        return try {
            parseOrThrow(number)
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    @JvmStatic
    fun isPriceZero(price: Price): Boolean {
        return price.priceAsFloat < EPSILON && price.priceAsFloat > -1 * EPSILON
    }
}