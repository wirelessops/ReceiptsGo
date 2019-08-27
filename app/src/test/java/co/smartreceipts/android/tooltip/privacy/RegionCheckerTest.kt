package co.smartreceipts.android.tooltip.privacy

import android.telephony.TelephonyManager
import co.smartreceipts.android.utils.TestLocaleToggler
import co.smartreceipts.android.utils.TestTimezoneToggler
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class RegionCheckerTest {

    private lateinit var regionChecker: RegionChecker

    @Mock
    private lateinit var telephonyManager: TelephonyManager

    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
        MockitoAnnotations.initMocks(this)
        regionChecker = RegionChecker(telephonyManager)
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun isInTheEuropeanUnionReturnsFalseForGSMPhonesWithUSNetworkCountryCode() {
        whenever(telephonyManager.phoneType).thenReturn(TelephonyManager.PHONE_TYPE_GSM)
        whenever(telephonyManager.networkCountryIso).thenReturn("US")
        assertFalse(regionChecker.isInTheEuropeanUnion())
    }

    @Test
    fun isInTheEuropeanUnionReturnsTrueForGSMPhonesWithDENetworkCountryCode() {
        whenever(telephonyManager.phoneType).thenReturn(TelephonyManager.PHONE_TYPE_GSM)
        whenever(telephonyManager.networkCountryIso).thenReturn("DE")
        assertTrue(regionChecker.isInTheEuropeanUnion())
    }

    @Test
    fun isInTheEuropeanUnionReturnsFalseForCDMAPhonesWithDENetworkCountryCodeInTheUSLocale() {
        whenever(telephonyManager.phoneType).thenReturn(TelephonyManager.PHONE_TYPE_CDMA)
        whenever(telephonyManager.networkCountryIso).thenReturn("DE")
        assertFalse(regionChecker.isInTheEuropeanUnion())
    }

    @Test
    fun isInTheEuropeanUnionReturnsFalseForGSMPhonesWithUSSimCountryCode() {
        whenever(telephonyManager.simCountryIso).thenReturn("US")
        assertFalse(regionChecker.isInTheEuropeanUnion())
    }

    @Test
    fun isInTheEuropeanUnionReturnsTrueForGSMPhonesWithDESimCountryCode() {
        whenever(telephonyManager.simCountryIso).thenReturn("DE")
        assertTrue(regionChecker.isInTheEuropeanUnion())
    }

    @Test
    fun isInTheEuropeanUnionReturnsFalseWhenNullTelephonyManagerDefaultsToUSLocale() {
        assertFalse(RegionChecker(null).isInTheEuropeanUnion())
    }

    @Test
    fun isInTheEuropeanUnionReturnsTrueWhenNullTelephonyManagerDefaultsToDELocale() {
        TestLocaleToggler.setDefaultLocale(Locale.GERMANY)
        assertTrue(RegionChecker(null).isInTheEuropeanUnion())
    }
}