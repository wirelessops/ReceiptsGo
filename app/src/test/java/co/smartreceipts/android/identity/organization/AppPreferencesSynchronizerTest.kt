package co.smartreceipts.android.identity.organization

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppPreferencesSynchronizerTest {

    // Class under test
    private lateinit var appPreferencesSynchronizer: AppPreferencesSynchronizer

    private val userPreferencesManager = mock<UserPreferenceManager>()

    @Before
    fun setUp() {
        appPreferencesSynchronizer = AppPreferencesSynchronizer(userPreferencesManager)

        whenever(userPreferencesManager.userPreferencesSingle).thenReturn(Single.just(UserPreference.values()))
    }

    @Test
    fun checkFloatPreferenceTest() {

        val preference = UserPreference.Receipts.MinimumReceiptPrice
        val preferenceName = ApplicationProvider.getApplicationContext<Context>().getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(10f)

        Assert.assertEquals(false, appPreferencesSynchronizer.checkPreferenceMatch(preferencesMap, preference).get())

        verify(userPreferencesManager, never()).set(eq(preference), any())

    }

    @Test
    fun applyFloatPreferenceWhenEqualsTest() {

        val preference = UserPreference.Receipts.MinimumReceiptPrice
        val preferenceName = ApplicationProvider.getApplicationContext<Context>().getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(5.5f)

        Assert.assertEquals(Optional.of(true), appPreferencesSynchronizer.applyPreference(preferencesMap, preference))

        verify(userPreferencesManager, never()).set(eq(preference), any())

    }

    @Test
    fun applyFloatPreferenceWhenNotEqualsTest() {

        val preference = UserPreference.Receipts.MinimumReceiptPrice
        val preferenceName = ApplicationProvider.getApplicationContext<Context>().getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(10f)

        Assert.assertEquals(Optional.of(false), appPreferencesSynchronizer.applyPreference(preferencesMap, preference))

        verify(userPreferencesManager, times(1)).set(eq(preference), any())
        verify(userPreferencesManager, times(1)).set(eq(preference), eq(5.5f))

    }

    @Test
    fun applyIntPreferenceTest() {

        val preference = UserPreference.General.DefaultReportDuration
        val preferenceName = ApplicationProvider.getApplicationContext<Context>().getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(8)

        Assert.assertEquals(Optional.of(false), appPreferencesSynchronizer.applyPreference(preferencesMap, preference))

        verify(userPreferencesManager, times(1)).set(eq(preference), any())
        verify(userPreferencesManager, times(1)).set(eq(preference), eq(6))

    }

    @Test
    fun applyBooleanPreferenceTest() {

        val preference = UserPreference.General.IncludeCostCenter
        val preferenceName = ApplicationProvider.getApplicationContext<Context>().getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(false)

        Assert.assertEquals(Optional.of(false), appPreferencesSynchronizer.applyPreference(preferencesMap, preference))

        verify(userPreferencesManager, times(1)).set(eq(preference), any())
        verify(userPreferencesManager, times(1)).set(eq(preference), eq(true))

    }

    @Test
    fun applyStringPreferenceTest() {

        val preference = UserPreference.General.DefaultCurrency
        val preferenceName = ApplicationProvider.getApplicationContext<Context>().getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn("another str")

        Assert.assertEquals(Optional.of(false), appPreferencesSynchronizer.applyPreference(preferencesMap, preference))

        verify(userPreferencesManager, times(1)).set(eq(preference), any())
        verify(userPreferencesManager, times(1)).set(eq(preference), eq("str"))

    }

    @Test
    fun checkOrganizationPreferencesWhenNotSameTest() {

        prepareForSimplifiedResponse()

        val preference1 = UserPreference.General.DefaultReportDuration
        val preference1Name = ApplicationProvider.getApplicationContext<Context>().getString(preference1.name)
        whenever(userPreferencesManager.name(eq(preference1))).thenReturn(preference1Name)
        whenever(userPreferencesManager.get(eq(preference1))).thenReturn(15)

        appPreferencesSynchronizer.checkOrganizationPreferencesMatch(preferencesMap).test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(false)
    }

    @Test
    fun checkOrganizationPreferencesWhenSameTest() {

        prepareForSimplifiedResponse()

        appPreferencesSynchronizer.checkOrganizationPreferencesMatch(preferencesMap).test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(true)
    }

    @Test
    fun applyOrganizationPreferencesTest() {

        prepareForSimplifiedResponse()

        val preference1 = UserPreference.General.DefaultReportDuration
        val preference1Name = ApplicationProvider.getApplicationContext<Context>().getString(preference1.name)
        whenever(userPreferencesManager.name(eq(preference1))).thenReturn(preference1Name)
        whenever(userPreferencesManager.get(eq(preference1))).thenReturn(15)

        appPreferencesSynchronizer.applyOrganizationPreferences(preferencesMap).test()
            .assertComplete()
            .assertNoErrors()

        verify(userPreferencesManager).set(eq(preference1), any())
        verify(userPreferencesManager).set(eq(preference1), eq(6))
        verify(userPreferencesManager, never()).set(eq(UserPreference.General.DefaultCurrency), any())
        verify(userPreferencesManager, never()).set(eq(UserPreference.General.IncludeCostCenter), any())
        verify(userPreferencesManager, never()).set(eq(UserPreference.Receipts.MinimumReceiptPrice), any())
    }

    @Test
    fun getAppPreferencesTest() {
        prepareForSimplifiedResponse()

        // check preferences with different types: Integer, String, Boolean, Float
        whenever(userPreferencesManager.userPreferencesSingle).thenReturn(
            Single.just(
                listOf(
                    UserPreference.General.DefaultReportDuration, UserPreference.General.DefaultCurrency,
                    UserPreference.General.IncludeCostCenter, UserPreference.Receipts.MinimumReceiptPrice
                )
            )
        )

        appPreferencesSynchronizer.getAppPreferences().test()
            .assertComplete()
            .assertNoErrors()
            .assertValue{result: Map<String, Any> -> result.toString() == preferencesMap.toString() }
    }


    private fun prepareForSimplifiedResponse() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val preference1 = UserPreference.General.DefaultReportDuration
        val preference1Name = context.getString(preference1.name)
        whenever(userPreferencesManager.name(eq(preference1))).thenReturn(preference1Name)
        whenever(userPreferencesManager.get(eq(preference1))).thenReturn(6)

        val preference2 = UserPreference.General.DefaultCurrency
        val preference2Name = context.getString(preference2.name)
        whenever(userPreferencesManager.name(eq(preference2))).thenReturn(preference2Name)
        whenever(userPreferencesManager.get(eq(preference2))).thenReturn("str")

        val preference3 = UserPreference.General.IncludeCostCenter
        val preference3Name = context.getString(preference3.name)
        whenever(userPreferencesManager.name(eq(preference3))).thenReturn(preference3Name)
        whenever(userPreferencesManager.get(eq(preference3))).thenReturn(true)

        val preference4 = UserPreference.Receipts.MinimumReceiptPrice
        val preference4Name = context.getString(preference4.name)
        whenever(userPreferencesManager.name(eq(preference4))).thenReturn(preference4Name)
        whenever(userPreferencesManager.get(eq(preference4))).thenReturn(5.5f)
    }


    companion object {

        private val preferencesMap: Map<String, Any> = mapOf(
            Pair("TripDuration", 6),
            Pair("isocurr", "str"),
            Pair("trackcostcenter", true),
            Pair("MinReceiptPrice", 5.5))

    }

}