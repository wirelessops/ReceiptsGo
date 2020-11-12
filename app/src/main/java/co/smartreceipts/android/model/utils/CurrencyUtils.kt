package co.smartreceipts.android.model.utils

import androidx.annotation.NonNull
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.model.Price
import org.joda.money.CurrencyUnit
import java.util.*

object CurrencyUtils {

    val currencies
        get() = _currencies

    fun isCurrencySupported(code: String): Boolean = currencies.find { it.currencyCode == code } != null

    private val _currencies: List<CurrencyWithDecimalPlaces> = getIso4217Currencies() + getNonIso4217Currencies()

    /**
     * Returns a list of all ISO 4127 currencies
     * http://en.wikipedia.org/wiki/ISO_4217
     *
     * Note: all these currencies are registered at the Joda Money by default, so, actually, no need to set decimal places
     *
     * @return a List<CurrencyWithDecimalPlaces> containing all ISO 4217 Currencies
     */
    private fun getIso4217Currencies(): List<CurrencyWithDecimalPlaces> {

        return listOf(
                CurrencyWithDecimalPlaces("AED"), // United Arab Emirates dirham
                CurrencyWithDecimalPlaces("AFN"), // Afghan afghani
                CurrencyWithDecimalPlaces("ALL"), // Albanian lek
                CurrencyWithDecimalPlaces("AMD"), // Armenian dram
                CurrencyWithDecimalPlaces("ANG"), // Netherlands Antillean guilder
                CurrencyWithDecimalPlaces("AOA"), // Angolan kwanza
                CurrencyWithDecimalPlaces("ARS"), // Argentine peso
                CurrencyWithDecimalPlaces("AUD"), // Australian dollar
                CurrencyWithDecimalPlaces("AWG"), // Aruban florin
                CurrencyWithDecimalPlaces("AZN"), // Azerbaijani manat
                CurrencyWithDecimalPlaces("BAM"), // Bosnia and Herzegovina convertible mark
                CurrencyWithDecimalPlaces("BBD"), // Barbados dollar
                CurrencyWithDecimalPlaces("BDT"), // Bangladeshi taka
                CurrencyWithDecimalPlaces("BGN"), // Bulgarian lev
                CurrencyWithDecimalPlaces("BHD"), // Bahraini dinar
                CurrencyWithDecimalPlaces("BIF"), // Burundian franc
                CurrencyWithDecimalPlaces("BMD"), // Bermudian dollar
                CurrencyWithDecimalPlaces("BND"), // Brunei dollar
                CurrencyWithDecimalPlaces("BOB"), // Boliviano
                CurrencyWithDecimalPlaces("BOV"), // Bolivian Mvdol (funds code)
                CurrencyWithDecimalPlaces("BRL"), // Brazilian real
                CurrencyWithDecimalPlaces("BSD"), // Bahamian dollar
                CurrencyWithDecimalPlaces("BTN"), // Bhutanese ngultrum
                CurrencyWithDecimalPlaces("BWP"), // Botswana pula
                CurrencyWithDecimalPlaces("BYN"), // Belarusian ruble
                CurrencyWithDecimalPlaces("BZD"), // Belize dollar
                CurrencyWithDecimalPlaces("CAD"), // Canadian dollar
                CurrencyWithDecimalPlaces("CDF"), // Congolese franc
                CurrencyWithDecimalPlaces("CHE"), // WIR Euro (complementary currency)
                CurrencyWithDecimalPlaces("CHF"), // Swiss franc
                CurrencyWithDecimalPlaces("CHW"), // WIR Franc (complementary currency)
                CurrencyWithDecimalPlaces("CLF"), // Unidad de Fomento (funds code)
                CurrencyWithDecimalPlaces("CLP"), // Chilean peso
                CurrencyWithDecimalPlaces("CNY"), // Chinese yuan
                CurrencyWithDecimalPlaces("COP"), // Colombian peso
                CurrencyWithDecimalPlaces("COU"), // Unidad de Valor Real (UVR) (funds code)[7]
                CurrencyWithDecimalPlaces("CRC"), // Costa Rican colon
                CurrencyWithDecimalPlaces("CUC"), // Cuban convertible peso
                CurrencyWithDecimalPlaces("CUP"), // Cuban peso
                CurrencyWithDecimalPlaces("CVE"), // Cape Verde escudo
                CurrencyWithDecimalPlaces("CZK"), // Czech koruna
                CurrencyWithDecimalPlaces("DJF"), // Djiboutian franc
                CurrencyWithDecimalPlaces("DKK"), // Danish krone
                CurrencyWithDecimalPlaces("DOP"), // Dominican peso
                CurrencyWithDecimalPlaces("DZD"), // Algerian dinar
                CurrencyWithDecimalPlaces("EGP"), // Egyptian pound
                CurrencyWithDecimalPlaces("ERN"), // Eritrean nakfa
                CurrencyWithDecimalPlaces("ETB"), // Ethiopian birr
                CurrencyWithDecimalPlaces("EUR"), // Euro
                CurrencyWithDecimalPlaces("FJD"), // Fiji dollar
                CurrencyWithDecimalPlaces("FKP"), // Falkland Islands pound
                CurrencyWithDecimalPlaces("GBP"), // Pound sterling
                CurrencyWithDecimalPlaces("GEL"), // Georgian lari
                CurrencyWithDecimalPlaces("GHS"), // Ghanaian cedi
                CurrencyWithDecimalPlaces("GIP"), // Gibraltar pound
                CurrencyWithDecimalPlaces("GMD"), // Gambian dalasi
                CurrencyWithDecimalPlaces("GNF"), // Guinean franc
                CurrencyWithDecimalPlaces("GTQ"), // Guatemalan quetzal
                CurrencyWithDecimalPlaces("GYD"), // Guyanese dollar
                CurrencyWithDecimalPlaces("HKD"), // Hong Kong dollar
                CurrencyWithDecimalPlaces("HNL"), // Honduran lempira
                CurrencyWithDecimalPlaces("HRK"), // Croatian kuna
                CurrencyWithDecimalPlaces("HTG"), // Haitian gourde
                CurrencyWithDecimalPlaces("HUF"), // Hungarian forint
                CurrencyWithDecimalPlaces("IDR"), // Indonesian rupiah
                CurrencyWithDecimalPlaces("ILS"), // Israeli new shekel
                CurrencyWithDecimalPlaces("INR"), // Indian rupee
                CurrencyWithDecimalPlaces("IQD"), // Iraqi dinar
                CurrencyWithDecimalPlaces("IRR"), // Iranian rial
                CurrencyWithDecimalPlaces("ISK"), // Icelandic króna
                CurrencyWithDecimalPlaces("JMD"), // Jamaican dollar
                CurrencyWithDecimalPlaces("JOD"), // Jordanian dinar
                CurrencyWithDecimalPlaces("JPY"), // Japanese yen
                CurrencyWithDecimalPlaces("KES"), // Kenyan shilling
                CurrencyWithDecimalPlaces("KGS"), // Kyrgyzstani som
                CurrencyWithDecimalPlaces("KHR"), // Cambodian riel
                CurrencyWithDecimalPlaces("KMF"), // Comoro franc
                CurrencyWithDecimalPlaces("KPW"), // North Korean won
                CurrencyWithDecimalPlaces("KRW"), // South Korean won
                CurrencyWithDecimalPlaces("KWD"), // Kuwaiti dinar
                CurrencyWithDecimalPlaces("KYD"), // Cayman Islands dollar
                CurrencyWithDecimalPlaces("KZT"), // Kazakhstani tenge
                CurrencyWithDecimalPlaces("LAK"), // Lao kip
                CurrencyWithDecimalPlaces("LBP"), // Lebanese pound
                CurrencyWithDecimalPlaces("LKR"), // Sri Lankan rupee
                CurrencyWithDecimalPlaces("LRD"), // Liberian dollar
                CurrencyWithDecimalPlaces("LSL"), // Lesotho loti
                CurrencyWithDecimalPlaces("LYD"), // Libyan dinar
                CurrencyWithDecimalPlaces("MAD"), // Moroccan dirham
                CurrencyWithDecimalPlaces("MDL"), // Moldovan leu
                CurrencyWithDecimalPlaces("MGA"), // Malagasy ariary
                CurrencyWithDecimalPlaces("MKD"), // Macedonian denar
                CurrencyWithDecimalPlaces("MMK"), // Myanmar kyat
                CurrencyWithDecimalPlaces("MNT"), // Mongolian tögrög
                CurrencyWithDecimalPlaces("MOP"), // Macanese pataca
                CurrencyWithDecimalPlaces("MRO"), // Mauritanian ouguiya
                CurrencyWithDecimalPlaces("MUR"), // Mauritian rupee
                CurrencyWithDecimalPlaces("MVR"), // Maldivian rufiyaa
                CurrencyWithDecimalPlaces("MWK"), // Malawian kwacha
                CurrencyWithDecimalPlaces("MXN"), // Mexican peso
                CurrencyWithDecimalPlaces("MXV"), // Mexican Unidad de Inversion (UDI) (funds code)
                CurrencyWithDecimalPlaces("MYR"), // Malaysian ringgit
                CurrencyWithDecimalPlaces("MZN"), // Mozambican metical
                CurrencyWithDecimalPlaces("NAD"), // Namibian dollar
                CurrencyWithDecimalPlaces("NGN"), // Nigerian naira
                CurrencyWithDecimalPlaces("NIO"), // Nicaraguan córdoba
                CurrencyWithDecimalPlaces("NOK"), // Norwegian krone
                CurrencyWithDecimalPlaces("NPR"), // Nepalese rupee
                CurrencyWithDecimalPlaces("NZD"), // New Zealand dollar
                CurrencyWithDecimalPlaces("OMR"), // Omani rial
                CurrencyWithDecimalPlaces("PAB"), // Panamanian balboa
                CurrencyWithDecimalPlaces("PEN"), // Peruvian Sol
                CurrencyWithDecimalPlaces("PGK"), // Papua New Guinean kina
                CurrencyWithDecimalPlaces("PHP"), // Philippine peso
                CurrencyWithDecimalPlaces("PKR"), // Pakistani rupee
                CurrencyWithDecimalPlaces("PLN"), // Polish złoty
                CurrencyWithDecimalPlaces("PYG"), // Paraguayan guaraní
                CurrencyWithDecimalPlaces("QAR"), // Qatari riyal
                CurrencyWithDecimalPlaces("RON"), // Romanian leu
                CurrencyWithDecimalPlaces("RSD"), // Serbian dinar
                CurrencyWithDecimalPlaces("RUB"), // Russian ruble
                CurrencyWithDecimalPlaces("RWF"), // Rwandan franc
                CurrencyWithDecimalPlaces("SAR"), // Saudi riyal
                CurrencyWithDecimalPlaces("SBD"), // Solomon Islands dollar
                CurrencyWithDecimalPlaces("SCR"), // Seychelles rupee
                CurrencyWithDecimalPlaces("SDG"), // Sudanese pound
                CurrencyWithDecimalPlaces("SEK"), // Swedish krona/kronor
                CurrencyWithDecimalPlaces("SGD"), // Singapore dollar
                CurrencyWithDecimalPlaces("SHP"), // Saint Helena pound
                CurrencyWithDecimalPlaces("SLL"), // Sierra Leonean leone
                CurrencyWithDecimalPlaces("SOS"), // Somali shilling
                CurrencyWithDecimalPlaces("SRD"), // Surinamese dollar
                CurrencyWithDecimalPlaces("SSP"), // South Sudanese pound
                CurrencyWithDecimalPlaces("STD"), // São Tomé and Príncipe dobra
                CurrencyWithDecimalPlaces("SVC"), // Salvadoran colón
                CurrencyWithDecimalPlaces("SYP"), // Syrian pound
                CurrencyWithDecimalPlaces("SZL"), // Swazi lilangeni
                CurrencyWithDecimalPlaces("THB"), // Thai baht
                CurrencyWithDecimalPlaces("TJS"), // Tajikistani somoni
                CurrencyWithDecimalPlaces("TMT"), // Turkmenistani manat
                CurrencyWithDecimalPlaces("TND"), // Tunisian dinar
                CurrencyWithDecimalPlaces("TOP"), // Tongan paʻanga
                CurrencyWithDecimalPlaces("TRY"), // Turkish lira
                CurrencyWithDecimalPlaces("TTD"), // Trinidad and Tobago dollar
                CurrencyWithDecimalPlaces("TWD"), // New Taiwan dollar
                CurrencyWithDecimalPlaces("TZS"), // Tanzanian shilling
                CurrencyWithDecimalPlaces("UAH"), // Ukrainian hryvnia
                CurrencyWithDecimalPlaces("UGX"), // Ugandan shilling
                CurrencyWithDecimalPlaces("USD"), // United States dollar
                CurrencyWithDecimalPlaces("USN"), // United States dollar (next day) (funds code)
                CurrencyWithDecimalPlaces("UYI"), // Uruguay Peso en Unidades Indexadas (URUIURUI) (funds code)
                CurrencyWithDecimalPlaces("UYU"), // Uruguayan peso
                CurrencyWithDecimalPlaces("UZS"), // Uzbekistan som
                CurrencyWithDecimalPlaces("VEF"), // Venezuelan bolívar
                CurrencyWithDecimalPlaces("VND"), // Vietnamese đồng
                CurrencyWithDecimalPlaces("VUV"), // Vanuatu vatu
                CurrencyWithDecimalPlaces("WST"), // Samoan tala
                CurrencyWithDecimalPlaces("XAF"), // CFA franc BEAC
                CurrencyWithDecimalPlaces("XAG"), // Silver (one troy ounce)
                CurrencyWithDecimalPlaces("XAU"), // Gold (one troy ounce)
                CurrencyWithDecimalPlaces("XBA"), // European Composite Unit (EURCO) (bond market unit)
                CurrencyWithDecimalPlaces("XBB"), // European Monetary Unit (E.M.U.-6) (bond market unit)
                CurrencyWithDecimalPlaces("XBC"), // European Unit of Account 9 (E.U.A.-9) (bond market unit)
                CurrencyWithDecimalPlaces("XBD"), // European Unit of Account 17 (E.U.A.-17) (bond market unit)
                CurrencyWithDecimalPlaces("XCD"), // East Caribbean dollar
                CurrencyWithDecimalPlaces("XDR"), // Special drawing rights
                CurrencyWithDecimalPlaces("XOF"), // CFA franc BCEAO
                CurrencyWithDecimalPlaces("XPD"), // Palladium (one troy ounce)
                CurrencyWithDecimalPlaces("XPF"), // CFP franc (franc Pacifique)
                CurrencyWithDecimalPlaces("XPT"), // Platinum (one troy ounce)
                CurrencyWithDecimalPlaces("XSU"), // SUCRE
                CurrencyWithDecimalPlaces("XTS"), // Code reserved for testing purposes
                CurrencyWithDecimalPlaces("XUA"), // ADB Unit of Account
                CurrencyWithDecimalPlaces("XXX"), // No currency
                CurrencyWithDecimalPlaces("YER"), // Yemeni rial
                CurrencyWithDecimalPlaces("ZAR"), // South African rand
                CurrencyWithDecimalPlaces("ZMW"), // Zambian kwacha
                CurrencyWithDecimalPlaces("ZWL") // Zimbabwean dollar A/10
        )
    }

    /**
     * Returns a list of non ISO 4217 Currency Codes (e.g. crypto-currencies, non-official ones, etc.) with decimal places
     * Mostly ones that have been requested over time.
     *
     * Note: decimal places are set for currencies that are not registered at the Joda Money dy default
     *
     *
     * https://en.wikipedia.org/wiki/ISO_4217#Non_ISO_4217_currencies
     *
     *
     * @return a [List] of extra currency codes with decimal places
     */
    private fun getNonIso4217Currencies(): List<CurrencyWithDecimalPlaces> {
        return listOf(

                // https://en.wikipedia.org/wiki/ISO_4217#Non_ISO_4217_currencies
                CurrencyWithDecimalPlaces("BYN", 2), // New Belarus Currency
                CurrencyWithDecimalPlaces("CNH", 2), // Chinese yuan (when traded offshore) - Hong Kong
                CurrencyWithDecimalPlaces("CNT"), // Chinese yuan (when traded offshore) - Taiwan
                CurrencyWithDecimalPlaces("GGP", 2), // Guernsey pound
                CurrencyWithDecimalPlaces("IMP", 2), // Isle of Man pound
                CurrencyWithDecimalPlaces("JEP", 2), // Jersey pound
                CurrencyWithDecimalPlaces("KID", 2), // Kiribati dollar
                CurrencyWithDecimalPlaces("NIS", 2), // New Israeli Shekel
                CurrencyWithDecimalPlaces("PRB", 2), // Transnistrian ruble
                CurrencyWithDecimalPlaces("SLS", 2), // Somaliland Shillings
                CurrencyWithDecimalPlaces("TVD", 2), // Tuvalu dollar

                // https://coinmarketcap.com/
                CurrencyWithDecimalPlaces("BTC", 8), // Bitcoin (Old Code)
                CurrencyWithDecimalPlaces("ETH", 18), // Ethereum
                CurrencyWithDecimalPlaces("GNT", 18), // Golem Project
                CurrencyWithDecimalPlaces("LTC", 8), // Litecoin
                CurrencyWithDecimalPlaces("PPC", 8), // Peercoin
                CurrencyWithDecimalPlaces("XBT", 8), // Bitcoin (New Code)
                CurrencyWithDecimalPlaces("XMR", 12), // Monero
                CurrencyWithDecimalPlaces("XRP", 6), // Ripple

                // Misc Requests from over the years:
                CurrencyWithDecimalPlaces("BYR", 0), // Belarusian ruble
                CurrencyWithDecimalPlaces("BSF", 2), // Venezuelan Bolivar
                CurrencyWithDecimalPlaces("DRC", 2), // Congolese Franc
                CurrencyWithDecimalPlaces("GHS", 2), // Ghanaian Cedi
                CurrencyWithDecimalPlaces("GST"), // Goods and Services Tax (Not sure how this got here but...?)
                CurrencyWithDecimalPlaces("LVL", 2), // Latvian lats (Replaced by Euro in 2014)
                CurrencyWithDecimalPlaces("LTL", 2), // Lithuanian litas (Replaced by Euro in 2015)
                CurrencyWithDecimalPlaces("XOF", 0), // West African CFA Franc
                CurrencyWithDecimalPlaces("XFU"), // UIC Franc (Replaced by Euro in 2013)
                CurrencyWithDecimalPlaces("ZMK", 2), // Zambian Kwacha
                CurrencyWithDecimalPlaces("ZWD", 2) // Zimbabwean Dollar
        )
    }

    @NonNull
    fun getDefaultCurrency(): CurrencyUnit? {
        return try {
            CurrencyUnit.of(Currency.getInstance(Locale.getDefault()).currencyCode)
        } catch (e: IllegalArgumentException) {
            Logger.warn(CurrencyUtils::class.java, "Unable to find a default currency, since the device has an unsupported ISO 3166 locale. Returning USD instead")
            CurrencyUnit.of("USD")
        }
    }
}

data class CurrencyWithDecimalPlaces(val currencyCode: String, val decimalPlaces: Int = Price.TOTAL_DECIMAL_PRECISION)