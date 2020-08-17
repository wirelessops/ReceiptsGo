package co.smartreceipts.android.model

import android.os.Parcelable
import co.smartreceipts.android.model.gson.ExchangeRate
import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit
import org.joda.money.format.MoneyAmountStyle
import org.joda.money.format.MoneyFormatter
import org.joda.money.format.MoneyFormatterBuilder
import java.math.BigDecimal
import java.util.*

/**
 * Defines a contract from which we can track the price value
 */
interface Price : Parcelable {

    companion object {
        /**
         * The decimal precision for price totals (ie two decimal points) like "$2.22" instead of "$2.22222"
         */
        const val TOTAL_DECIMAL_PRECISION = 2

        /**
         * Defines the default precision rate that we use for rounding off our multiplied values (in
         * conjunction with the exchange rates)
         */
        const val ROUNDING_PRECISION = 5

        val moneyFormatter: MoneyFormatter
            get() {
                if (_moneyFormatter.locale != Locale.getDefault()) {
                    _moneyFormatter = MoneyFormatterBuilder()
                        .appendAmountLocalized()
                        .toFormatter()
                }
                return _moneyFormatter
            }

        private var _moneyFormatter = MoneyFormatterBuilder()
            .appendAmountLocalized()
            .toFormatter()

    }

    val money: BigMoney

    val currency: CurrencyUnit

    /**
     * Gets the exchange rate associated with this particular price object, which we can use to attempt to convert this
     * price from one currency to another
     *
     * @return the [ExchangeRate]
     */
    val exchangeRate: ExchangeRate

    /**
     * Gets the float representation of this price
     *
     * @return the float primitive, which represents the total price of this receipt
     */
    val priceAsFloat: Float

    /**
     * Gets the [BigDecimal] representation of this price
     *
     * @return the [BigDecimal] representation of this price
     */
    val price: BigDecimal

    /**
     * A "decimal-formatted" price, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2"
     *
     * @return the decimal formatted price [String]
     */
    val decimalFormattedPrice: String

    /**
     * The "currency-formatted" price, which would appear as "$25.20" or "$25,20" as determined by the user's locale
     *
     * @return - the currency formatted price [String]
     */
    val currencyFormattedPrice: String

    /**
     * The "currency-code-formatted" price, which would appear as "USD25.20" or "USD25,20" as determined by the user's locale
     *
     * @return - the currency formatted price [String]
     */
    val currencyCodeFormattedPrice: String

    /**
     * Gets the currency code representation for this price or [PriceCurrency.MISSING_CURRENCY]
     * if it cannot be found
     *
     * @return the currency code [String] for this price
     */
    val currencyCode: String

    val isSingleCurrency: Boolean

}