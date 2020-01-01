package co.smartreceipts.android.tooltip.privacy

import android.content.Context
import android.telephony.TelephonyManager
import co.smartreceipts.core.di.scopes.ApplicationScope
import java.util.*
import javax.inject.Inject


/**
 * Allows us to quickly check a specific region for scenarios in which app business logic differs as
 * per the user's region
 */
@ApplicationScope
class RegionChecker internal constructor(private val telephonyManager: TelephonyManager?) {

    @Inject
    constructor(context: Context) : this(context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)

    /**
     * Checks if the user's phone is an EU model (useful for GDPR prompts)
     *
     * @return true if this is an EU model phone. false otherwise
     */
    fun isInTheEuropeanUnion() : Boolean {
        return EU_COUNTRY_CODES.contains(getUserIso3166CountryCode())
    }

    /**
     * Gets the two-character country iso code for the current user based on a combination of their
     * telephone network, SIM card, and default locale
     *
     * @return the Iso 3166 country country code
     */
    private fun getUserIso3166CountryCode() : String {
        // First, check the country code from the mobile network
        // Note: The docs state that this may be unreliable on CDMA networks, so we ignore this check here
        if (telephonyManager?.phoneType != TelephonyManager.PHONE_TYPE_CDMA) {
            val networkCountryIso = telephonyManager?.networkCountryIso
            if (networkCountryIso?.isNotEmpty() == true) {
                return networkCountryIso.toUpperCase(Locale.US)
            }
        }

        // Next, check it from the SIM card
        val simCountryIso = telephonyManager?.simCountryIso
        if (simCountryIso?.isNotEmpty() == true) {
            return simCountryIso.toUpperCase(Locale.US)
        }

        // Finally, return the current locale
        return Locale.getDefault().country.toUpperCase(Locale.US)
    }

    companion object {
        /**
         * This array contains a list of all known EU country codes
         *
         * From: https://ec.europa.eu/eurostat/statistics-explained/index.php/Tutorial:Country_codes_and_protocol_order
         */
        private val EU_COUNTRY_CODES = arrayListOf(
                "BE",
                "BG",
                "CZ",
                "DK",
                "DE",
                "EE",
                "IE",
                "EL",
                "ES",
                "FR",
                "HR",
                "IT",
                "CY",
                "LV",
                "LT",
                "LU",
                "HU",
                "MT",
                "NL",
                "AT",
                "PL",
                "PT",
                "RO",
                "SI",
                "SK",
                "FI",
                "SE",
                "UK",
                "IS",
                "LI",
                "NO",
                "CH"
        )
    }
}