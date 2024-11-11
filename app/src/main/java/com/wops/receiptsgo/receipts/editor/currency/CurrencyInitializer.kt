package com.wops.receiptsgo.receipts.editor.currency

import com.wops.analytics.log.Logger
import com.wops.receiptsgo.model.utils.CurrencyUtils
import org.joda.money.CurrencyUnit
import org.joda.money.IllegalCurrencyException
import javax.inject.Inject

class CurrencyInitializer @Inject constructor() {

    /**
     * Register currencies from CurrencyUtils to Joda Money
     */
    fun init() {
        Logger.debug(this, "registered currencies size ${CurrencyUnit.registeredCurrencies().size}")

        CurrencyUtils.currencies.forEach {
            try {
                if (it.currencyCode.length == 3) {
                    CurrencyUnit.of(it.currencyCode)
                } else {
                    Logger.debug(this, "Currency code must have 3 symbols. Skipping ${it.currencyCode}")
                }
            } catch (e: IllegalCurrencyException) {
                val registeredCurrency = CurrencyUnit.registerCurrency(it.currencyCode, -1, it.decimalPlaces, false)
            }
        }

        Logger.debug(this, "registered currencies size ${CurrencyUnit.registeredCurrencies().size}")
    }
}