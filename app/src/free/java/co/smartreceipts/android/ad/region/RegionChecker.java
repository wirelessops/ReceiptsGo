package co.smartreceipts.android.ad.region;

import android.content.Context;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;

@ApplicationScope
public class RegionChecker {

    private static final List<String> EU_ISO_3166_COUNTRIES = Arrays.asList("AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB", "GR", "HR", "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK", "UK");

    private final Context context;

    @Inject
    public RegionChecker(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context);
    }

    public boolean isInWesternEurope() {
        final String iso3166CountryCode = getUserCountry(context);
        return iso3166CountryCode != null && EU_ISO_3166_COUNTRIES.contains(iso3166CountryCode);
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or {@code null} if not available)
     *
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or {@code null}
     */
    private String getUserCountry(@NonNull Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception e) {
            // Intentional no-op to avoid crash
        }
        return null;
    }


}
