package co.smartreceipts.android.model.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.utils.sorting.AlphabeticalCaseInsensitiveCharSequenceComparator;

public class CurrencyUtils {

    @NonNull
    public static List<String> getAllCurrencies() {
        final List<String> currencies = new ArrayList<>();
        currencies.addAll(getIso4217CurrencyCodes());
        currencies.addAll(getNonIso4217CurrencyCodes());
        Collections.sort(currencies, new AlphabeticalCaseInsensitiveCharSequenceComparator());
        return currencies;
    }

    /**
     * Returns a list of all ISO 4127 currencies
     * http://en.wikipedia.org/wiki/ISO_4217
     *
     * @return a List<String> containing all ISO 4217 Currencies
     */
    @NonNull
    private static List<String> getIso4217CurrencyCodes() {
        final ArrayList<String> iso4217Currencies = new ArrayList<>();
        iso4217Currencies.add("AED"); // United Arab Emirates dirham
        iso4217Currencies.add("AFN"); // Afghan afghani
        iso4217Currencies.add("ALL"); // Albanian lek
        iso4217Currencies.add("AMD"); // Armenian dram
        iso4217Currencies.add("ANG"); // Netherlands Antillean guilder
        iso4217Currencies.add("AOA"); // Angolan kwanza
        iso4217Currencies.add("ARS"); // Argentine peso
        iso4217Currencies.add("AUD"); // Australian dollar
        iso4217Currencies.add("AWG"); // Aruban florin
        iso4217Currencies.add("AZN"); // Azerbaijani manat
        iso4217Currencies.add("BAM"); // Bosnia and Herzegovina convertible mark
        iso4217Currencies.add("BBD"); // Barbados dollar
        iso4217Currencies.add("BDT"); // Bangladeshi taka
        iso4217Currencies.add("BGN"); // Bulgarian lev
        iso4217Currencies.add("BHD"); // Bahraini dinar
        iso4217Currencies.add("BIF"); // Burundian franc
        iso4217Currencies.add("BMD"); // Bermudian dollar
        iso4217Currencies.add("BND"); // Brunei dollar
        iso4217Currencies.add("BOB"); // Boliviano
        iso4217Currencies.add("BOV"); // Bolivian Mvdol (funds code)
        iso4217Currencies.add("BRL"); // Brazilian real
        iso4217Currencies.add("BSD"); // Bahamian dollar
        iso4217Currencies.add("BTN"); // Bhutanese ngultrum
        iso4217Currencies.add("BWP"); // Botswana pula
        iso4217Currencies.add("BYN"); // Belarusian ruble
        iso4217Currencies.add("BZD"); // Belize dollar
        iso4217Currencies.add("CAD"); // Canadian dollar
        iso4217Currencies.add("CDF"); // Congolese franc
        iso4217Currencies.add("CHE"); // WIR Euro (complementary currency)
        iso4217Currencies.add("CHF"); // Swiss franc
        iso4217Currencies.add("CHW"); // WIR Franc (complementary currency)
        iso4217Currencies.add("CLF"); // Unidad de Fomento (funds code)
        iso4217Currencies.add("CLP"); // Chilean peso
        iso4217Currencies.add("CNY"); // Chinese yuan
        iso4217Currencies.add("COP"); // Colombian peso
        iso4217Currencies.add("COU"); // Unidad de Valor Real (UVR) (funds code)[7]
        iso4217Currencies.add("CRC"); // Costa Rican colon
        iso4217Currencies.add("CUC"); // Cuban convertible peso
        iso4217Currencies.add("CUP"); // Cuban peso
        iso4217Currencies.add("CVE"); // Cape Verde escudo
        iso4217Currencies.add("CZK"); // Czech koruna
        iso4217Currencies.add("DJF"); // Djiboutian franc
        iso4217Currencies.add("DKK"); // Danish krone
        iso4217Currencies.add("DOP"); // Dominican peso
        iso4217Currencies.add("DZD"); // Algerian dinar
        iso4217Currencies.add("EGP"); // Egyptian pound
        iso4217Currencies.add("ERN"); // Eritrean nakfa
        iso4217Currencies.add("ETB"); // Ethiopian birr
        iso4217Currencies.add("EUR"); // Euro
        iso4217Currencies.add("FJD"); // Fiji dollar
        iso4217Currencies.add("FKP"); // Falkland Islands pound
        iso4217Currencies.add("GBP"); // Pound sterling
        iso4217Currencies.add("GEL"); // Georgian lari
        iso4217Currencies.add("GHS"); // Ghanaian cedi
        iso4217Currencies.add("GIP"); // Gibraltar pound
        iso4217Currencies.add("GMD"); // Gambian dalasi
        iso4217Currencies.add("GNF"); // Guinean franc
        iso4217Currencies.add("GTQ"); // Guatemalan quetzal
        iso4217Currencies.add("GYD"); // Guyanese dollar
        iso4217Currencies.add("HKD"); // Hong Kong dollar
        iso4217Currencies.add("HNL"); // Honduran lempira
        iso4217Currencies.add("HRK"); // Croatian kuna
        iso4217Currencies.add("HTG"); // Haitian gourde
        iso4217Currencies.add("HUF"); // Hungarian forint
        iso4217Currencies.add("IDR"); // Indonesian rupiah
        iso4217Currencies.add("ILS"); // Israeli new shekel
        iso4217Currencies.add("INR"); // Indian rupee
        iso4217Currencies.add("IQD"); // Iraqi dinar
        iso4217Currencies.add("IRR"); // Iranian rial
        iso4217Currencies.add("ISK"); // Icelandic króna
        iso4217Currencies.add("JMD"); // Jamaican dollar
        iso4217Currencies.add("JOD"); // Jordanian dinar
        iso4217Currencies.add("JPY"); // Japanese yen
        iso4217Currencies.add("KES"); // Kenyan shilling
        iso4217Currencies.add("KGS"); // Kyrgyzstani som
        iso4217Currencies.add("KHR"); // Cambodian riel
        iso4217Currencies.add("KMF"); // Comoro franc
        iso4217Currencies.add("KPW"); // North Korean won
        iso4217Currencies.add("KRW"); // South Korean won
        iso4217Currencies.add("KWD"); // Kuwaiti dinar
        iso4217Currencies.add("KYD"); // Cayman Islands dollar
        iso4217Currencies.add("KZT"); // Kazakhstani tenge
        iso4217Currencies.add("LAK"); // Lao kip
        iso4217Currencies.add("LBP"); // Lebanese pound
        iso4217Currencies.add("LKR"); // Sri Lankan rupee
        iso4217Currencies.add("LRD"); // Liberian dollar
        iso4217Currencies.add("LSL"); // Lesotho loti
        iso4217Currencies.add("LYD"); // Libyan dinar
        iso4217Currencies.add("MAD"); // Moroccan dirham
        iso4217Currencies.add("MDL"); // Moldovan leu
        iso4217Currencies.add("MGA"); // Malagasy ariary
        iso4217Currencies.add("MKD"); // Macedonian denar
        iso4217Currencies.add("MMK"); // Myanmar kyat
        iso4217Currencies.add("MNT"); // Mongolian tögrög
        iso4217Currencies.add("MOP"); // Macanese pataca
        iso4217Currencies.add("MRO"); // Mauritanian ouguiya
        iso4217Currencies.add("MUR"); // Mauritian rupee
        iso4217Currencies.add("MVR"); // Maldivian rufiyaa
        iso4217Currencies.add("MWK"); // Malawian kwacha
        iso4217Currencies.add("MXN"); // Mexican peso
        iso4217Currencies.add("MXV"); // Mexican Unidad de Inversion (UDI) (funds code)
        iso4217Currencies.add("MYR"); // Malaysian ringgit
        iso4217Currencies.add("MZN"); // Mozambican metical
        iso4217Currencies.add("NAD"); // Namibian dollar
        iso4217Currencies.add("NGN"); // Nigerian naira
        iso4217Currencies.add("NIO"); // Nicaraguan córdoba
        iso4217Currencies.add("NOK"); // Norwegian krone
        iso4217Currencies.add("NPR"); // Nepalese rupee
        iso4217Currencies.add("NZD"); // New Zealand dollar
        iso4217Currencies.add("OMR"); // Omani rial
        iso4217Currencies.add("PAB"); // Panamanian balboa
        iso4217Currencies.add("PEN"); // Peruvian Sol
        iso4217Currencies.add("PGK"); // Papua New Guinean kina
        iso4217Currencies.add("PHP"); // Philippine peso
        iso4217Currencies.add("PKR"); // Pakistani rupee
        iso4217Currencies.add("PLN"); // Polish złoty
        iso4217Currencies.add("PYG"); // Paraguayan guaraní
        iso4217Currencies.add("QAR"); // Qatari riyal
        iso4217Currencies.add("RON"); // Romanian leu
        iso4217Currencies.add("RSD"); // Serbian dinar
        iso4217Currencies.add("RUB"); // Russian ruble
        iso4217Currencies.add("RWF"); // Rwandan franc
        iso4217Currencies.add("SAR"); // Saudi riyal
        iso4217Currencies.add("SBD"); // Solomon Islands dollar
        iso4217Currencies.add("SCR"); // Seychelles rupee
        iso4217Currencies.add("SDG"); // Sudanese pound
        iso4217Currencies.add("SEK"); // Swedish krona/kronor
        iso4217Currencies.add("SGD"); // Singapore dollar
        iso4217Currencies.add("SHP"); // Saint Helena pound
        iso4217Currencies.add("SLL"); // Sierra Leonean leone
        iso4217Currencies.add("SOS"); // Somali shilling
        iso4217Currencies.add("SRD"); // Surinamese dollar
        iso4217Currencies.add("SSP"); // South Sudanese pound
        iso4217Currencies.add("STD"); // São Tomé and Príncipe dobra
        iso4217Currencies.add("SVC"); // Salvadoran colón
        iso4217Currencies.add("SYP"); // Syrian pound
        iso4217Currencies.add("SZL"); // Swazi lilangeni
        iso4217Currencies.add("THB"); // Thai baht
        iso4217Currencies.add("TJS"); // Tajikistani somoni
        iso4217Currencies.add("TMT"); // Turkmenistani manat
        iso4217Currencies.add("TND"); // Tunisian dinar
        iso4217Currencies.add("TOP"); // Tongan paʻanga
        iso4217Currencies.add("TRY"); // Turkish lira
        iso4217Currencies.add("TTD"); // Trinidad and Tobago dollar
        iso4217Currencies.add("TWD"); // New Taiwan dollar
        iso4217Currencies.add("TZS"); // Tanzanian shilling
        iso4217Currencies.add("UAH"); // Ukrainian hryvnia
        iso4217Currencies.add("UGX"); // Ugandan shilling
        iso4217Currencies.add("USD"); // United States dollar
        iso4217Currencies.add("USN"); // United States dollar (next day) (funds code)
        iso4217Currencies.add("UYI"); // Uruguay Peso en Unidades Indexadas (URUIURUI) (funds code)
        iso4217Currencies.add("UYU"); // Uruguayan peso
        iso4217Currencies.add("UZS"); // Uzbekistan som
        iso4217Currencies.add("VEF"); // Venezuelan bolívar
        iso4217Currencies.add("VND"); // Vietnamese đồng
        iso4217Currencies.add("VUV"); // Vanuatu vatu
        iso4217Currencies.add("WST"); // Samoan tala
        iso4217Currencies.add("XAF"); // CFA franc BEAC
        iso4217Currencies.add("XAG"); // Silver (one troy ounce)
        iso4217Currencies.add("XAU"); // Gold (one troy ounce)
        iso4217Currencies.add("XBA"); // European Composite Unit (EURCO) (bond market unit)
        iso4217Currencies.add("XBB"); // European Monetary Unit (E.M.U.-6) (bond market unit)
        iso4217Currencies.add("XBC"); // European Unit of Account 9 (E.U.A.-9) (bond market unit)
        iso4217Currencies.add("XBD"); // European Unit of Account 17 (E.U.A.-17) (bond market unit)
        iso4217Currencies.add("XCD"); // East Caribbean dollar
        iso4217Currencies.add("XDR"); // Special drawing rights
        iso4217Currencies.add("XOF"); // CFA franc BCEAO
        iso4217Currencies.add("XPD"); // Palladium (one troy ounce)
        iso4217Currencies.add("XPF"); // CFP franc (franc Pacifique)
        iso4217Currencies.add("XPT"); // Platinum (one troy ounce)
        iso4217Currencies.add("XSU"); // SUCRE
        iso4217Currencies.add("XTS"); // Code reserved for testing purposes
        iso4217Currencies.add("XUA"); // ADB Unit of Account
        iso4217Currencies.add("XXX"); // No currency
        iso4217Currencies.add("YER"); // Yemeni rial
        iso4217Currencies.add("ZAR"); // South African rand
        iso4217Currencies.add("ZMW"); // Zambian kwacha
        iso4217Currencies.add("ZWL"); // Zimbabwean dollar A/10
        return iso4217Currencies;
    }

    /**
     * Returns a list of non ISO 4217 Currency Codes (e.g. crypto-currencies, non-official ones, etc.)
     * Mostly ones that have been requested over time.
     * <p>
     * https://en.wikipedia.org/wiki/ISO_4217#Non_ISO_4217_currencies
     * </p>
     *
     * @return a {@link List} of extra currency codes
     */
    @NonNull
    public static List<String> getNonIso4217CurrencyCodes() {
        final ArrayList<String> nonIso4217Currencies = new ArrayList<>();

        // https://en.wikipedia.org/wiki/ISO_4217#Non_ISO_4217_currencies
        nonIso4217Currencies.add("BYN");  // New Belarus Currency
        nonIso4217Currencies.add("CNH");  // Chinese yuan (when traded offshore) - Hong Kong
        nonIso4217Currencies.add("CNT");  // Chinese yuan (when traded offshore) - Taiwan
        nonIso4217Currencies.add("GGP");  // Guernsey pound
        nonIso4217Currencies.add("IMP");  // Isle of Man pound
        nonIso4217Currencies.add("JEP");  // Jersey pound
        nonIso4217Currencies.add("KID");  // Kiribati dollar
        nonIso4217Currencies.add("NIS");  // New Israeli Shekel
        nonIso4217Currencies.add("PRB");  // Transnistrian ruble
        nonIso4217Currencies.add("SLS");  // Somaliland Shillings
        nonIso4217Currencies.add("TVD");  // Tuvalu dollar

        // https://coinmarketcap.com/
        nonIso4217Currencies.add("BTC");  // Bitcoin (Old Code)
        nonIso4217Currencies.add("DOGE"); // Dogecoin
        nonIso4217Currencies.add("ETH");  // Etherium
        nonIso4217Currencies.add("GNT");  // Golem Project
        nonIso4217Currencies.add("LTC");  // Litecoin
        nonIso4217Currencies.add("PPC");  // Peercoin
        nonIso4217Currencies.add("SC");   // SiaCoin
        nonIso4217Currencies.add("SJCX"); // Storjcoin
        nonIso4217Currencies.add("XBT");  // Bitcoin (New Code)
        nonIso4217Currencies.add("XMR");  // Monero
        nonIso4217Currencies.add("XRP");  // Ripple

        // Misc Requests from over the years:
        nonIso4217Currencies.add("BYR");  // Belarusian ruble
        nonIso4217Currencies.add("BSF");  // Venezuelan Bolivar
        nonIso4217Currencies.add("DRC");  // Congolese Franc
        nonIso4217Currencies.add("GHS");  // Ghanaian Cedi
        nonIso4217Currencies.add("GST");  // Goods and Services Tax (Not sure how this got here but...?)
        nonIso4217Currencies.add("LVL");  // Latvian lats (Replaced by Euro in 2014)
        nonIso4217Currencies.add("LTL");  // Lithuanian litas (Replaced by Euro in 2015)
        nonIso4217Currencies.add("XOF");  // West African CFA Franc
        nonIso4217Currencies.add("XFU");  // UIC Franc (Replaced by Euro in 2013)
        nonIso4217Currencies.add("ZMK");  // Zambian Kwacha
        nonIso4217Currencies.add("ZWD");  // Zimbabwean Dollar
        return nonIso4217Currencies;
    }
}
