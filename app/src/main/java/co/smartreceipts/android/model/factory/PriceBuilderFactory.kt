package co.smartreceipts.android.model.factory

import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Priceable
import co.smartreceipts.android.model.gson.ExchangeRate
import co.smartreceipts.android.model.impl.*
import co.smartreceipts.android.model.utils.ModelUtils
import org.joda.money.CurrencyUnit
import java.math.BigDecimal
import java.util.*

/**
 * A [Price] [BuilderFactory]
 * implementation, which will be used to generate instances of [Price] objects
 */
class PriceBuilderFactory : BuilderFactory<Price> {
    private var priceDecimal: BigDecimal = BigDecimal.ZERO
    private var currency: CurrencyUnit = CurrencyUnit.of(Locale.getDefault())
    private var prices: List<Price> = emptyList()
    private var exchangeRate: ExchangeRate? = null

    constructor() {
        currency = CurrencyUnit.of(Locale.getDefault())
        priceDecimal = BigDecimal.ZERO
    }

    constructor(price: Price) {
        priceDecimal = price.price
        currency = price.currency
        exchangeRate = price.exchangeRate
    }

    fun setPrice(price: Price): PriceBuilderFactory {
        priceDecimal = price.price
        currency = price.currency
        exchangeRate = price.exchangeRate

        prices = emptyList()
        return this
    }

    fun setPrice(price: String): PriceBuilderFactory {
        priceDecimal = ModelUtils.tryParse(price)

        prices = emptyList()
        return this
    }

    fun setPrice(price: Double): PriceBuilderFactory {
        priceDecimal = BigDecimal.valueOf(price)

        prices = emptyList()
        return this
    }

    fun setPrice(price: BigDecimal): PriceBuilderFactory {
        priceDecimal = price

        prices = emptyList()
        return this
    }

    fun setCurrency(currency: CurrencyUnit): PriceBuilderFactory {
        this.currency = currency

        return this
    }

    fun setCurrency(currencyCode: String): PriceBuilderFactory {
        currency = CurrencyUnit.of(currencyCode)

        return this
    }

    fun setExchangeRate(exchangeRate: ExchangeRate): PriceBuilderFactory {
        this.exchangeRate = exchangeRate

        return this
    }

    fun setPrices(prices: List<Price>, desiredCurrency: CurrencyUnit): PriceBuilderFactory {
        this.prices = ArrayList(prices)
        currency = desiredCurrency

        return this
    }

    fun setPriceables(priceables: List<Priceable>, desiredCurrency: CurrencyUnit): PriceBuilderFactory {
        prices = priceables.map { it.price }

        currency = desiredCurrency
        return this
    }

    override fun build(): Price {

        return when {
            prices.isNotEmpty() -> MultiplePriceImpl(currency, prices)

            else -> {
                val rate = exchangeRate ?: ExchangeRateBuilderFactory().setBaseCurrency(currency).build()
                SinglePriceImpl(priceDecimal, currency, rate)
            }
        }
    }
}