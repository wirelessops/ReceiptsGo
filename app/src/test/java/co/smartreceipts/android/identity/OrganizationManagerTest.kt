package co.smartreceipts.android.identity

import co.smartreceipts.android.apis.ApiValidationException
import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.config.ConfigurationManager
import co.smartreceipts.android.identity.apis.organizations.*
import co.smartreceipts.android.identity.store.MutableIdentityStore
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.ConfigurableResourceFeature
import com.hadisatrio.optional.Optional
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.Single
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class OrganizationManagerTest {

    // Class under test
    private lateinit var organizationManager: OrganizationManager

    private val webServiceManager = mock<WebServiceManager>()
    private val identityStore = mock<MutableIdentityStore>()
    private val userPreferencesManager = mock<UserPreferenceManager>()
    private val configurationManager = mock<ConfigurationManager>()
    private val service = mock<OrganizationsService>()
    private val organizationsResponse = mock<OrganizationsResponse>()

    private val organization1 = mock<Organization>()
    private val organization2 = mock<Organization>()
    private val orgError = mock<Error>()

    private val organizationSettings = AppSettings.OrganizationSettings(settingsJsonObject)


    @Before
    fun setUp() {
        organizationManager = OrganizationManager(webServiceManager, identityStore, userPreferencesManager, configurationManager)

        whenever(configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)).thenReturn(true)
        whenever(identityStore.isLoggedIn).thenReturn(true)

        whenever(webServiceManager.getService(OrganizationsService::class.java)).thenReturn(service)
        whenever(service.organizations()).thenReturn(Observable.just(organizationsResponse))

        whenever(organizationsResponse.organizations).thenReturn(listOf(organization1))
        whenever(organization1.error).thenReturn(orgError)
        whenever(orgError.hasError).thenReturn(false)

        val appSettings = mock<AppSettings>()
        whenever(organization1.appSettings).thenReturn(appSettings)
        whenever(appSettings.settings).thenReturn(organizationSettings)

        whenever(userPreferencesManager.userPreferencesSingle).thenReturn(Single.just(UserPreference.values()))
    }

    @Test
    fun getPrimaryOrganizationTestWithoutOrganizationSyncing() {
        whenever(configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)).thenReturn(false)

        organizationManager.getPrimaryOrganization().test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()
            .assertResult()
    }

    @Test
    fun getPrimaryOrganizationTestWithoutLoggingIn() {
        whenever(identityStore.isLoggedIn).thenReturn(false)

        organizationManager.getPrimaryOrganization().test()
            .assertNotComplete()
            .assertError(IllegalStateException::class.java)
            .assertNoValues()
    }

    @Test
    fun getPrimaryOrganizationTestWithServiceError() {

        whenever(service.organizations()).thenReturn(Observable.error(Exception()))

        organizationManager.getPrimaryOrganization().test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(Exception::class.java)
    }

    @Test
    fun getPrimaryOrganizationTestWithNoOrganizations() {

        whenever(organizationsResponse.organizations).thenReturn(emptyList())

        organizationManager.getPrimaryOrganization().test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()
            .assertResult()
    }

    @Test
    fun getPrimaryOrganizationTestWithFewOrganizations() {

        whenever(organizationsResponse.organizations).thenReturn(listOf(organization1, organization2))

        organizationManager.getPrimaryOrganization().test()
            .assertComplete()
            .assertNoErrors()
            .assertValueCount(1)
            .assertResult(organization1)
    }

    @Test
    fun getPrimaryOrganizationTestWithOneOrganization() {

        organizationManager.getPrimaryOrganization().test()
            .assertComplete()
            .assertNoErrors()
            .assertValueCount(1)
            .assertResult(organization1)
    }

    @Test
    fun getPrimaryOrganizationTestWithOrganizationError() {

        whenever(organizationsResponse.organizations).thenReturn(listOf(organization1))
        whenever(orgError.hasError).thenReturn(true)

        organizationManager.getPrimaryOrganization().test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(ApiValidationException::class.java)
    }

    @Test
    fun justCheckSingleFloatSettingTest() {

        val preference = UserPreference.Receipts.MinimumReceiptPrice
        val preferenceName = RuntimeEnvironment.application.getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(10f)

        assertEquals(Optional.of(false), organizationManager.checkPreferenceMatch(organizationSettings, preference, false))

        verify(userPreferencesManager, never()).set(eq(preference), any())

    }

    @Test
    fun applySingleEqualsFloatSettingTest() {

        val preference = UserPreference.Receipts.MinimumReceiptPrice
        val preferenceName = RuntimeEnvironment.application.getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(5.5f)

        assertEquals(Optional.of(true), organizationManager.checkPreferenceMatch(organizationSettings, preference, true))

        verify(userPreferencesManager, never()).set(eq(preference), any())

    }

    @Test
    fun applySingleFloatSettingTest() {

        val preference = UserPreference.Receipts.MinimumReceiptPrice
        val preferenceName = RuntimeEnvironment.application.getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(10f)

        assertEquals(Optional.of(false), organizationManager.checkPreferenceMatch(organizationSettings, preference, true))

        verify(userPreferencesManager, times(1)).set(eq(preference), any())
        verify(userPreferencesManager, times(1)).set(eq(preference), eq(5.5f))

    }

    @Test
    fun applySingleIntSettingTest() {

        val preference = UserPreference.General.DefaultReportDuration
        val preferenceName = RuntimeEnvironment.application.getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(8)

        assertEquals(Optional.of(false), organizationManager.checkPreferenceMatch(organizationSettings, preference, true))

        verify(userPreferencesManager, times(1)).set(eq(preference), any())
        verify(userPreferencesManager, times(1)).set(eq(preference), eq(6))

    }

    @Test
    fun applySingleBooleanSettingTest() {

        val preference = UserPreference.General.IncludeCostCenter
        val preferenceName = RuntimeEnvironment.application.getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn(false)

        assertEquals(Optional.of(false), organizationManager.checkPreferenceMatch(organizationSettings, preference, true))

        verify(userPreferencesManager, times(1)).set(eq(preference), any())
        verify(userPreferencesManager, times(1)).set(eq(preference), eq(true))

    }

    @Test
    fun applySingleStringSettingTest() {

        val preference = UserPreference.General.DefaultCurrency
        val preferenceName = RuntimeEnvironment.application.getString(preference.name)
        whenever(userPreferencesManager.name(eq(preference))).thenReturn(preferenceName)
        whenever(userPreferencesManager.get(eq(preference))).thenReturn("another str")

        assertEquals(Optional.of(false), organizationManager.checkPreferenceMatch(organizationSettings, preference, true))

        verify(userPreferencesManager, times(1)).set(eq(preference), any())
        verify(userPreferencesManager, times(1)).set(eq(preference), eq("str"))

    }

    private fun prepareForSimplifiedResponse() {
        val appSettings = mock<AppSettings>()
        whenever(organization2.appSettings).thenReturn(appSettings)
        whenever(appSettings.settings).thenReturn(AppSettings.OrganizationSettings(simplifiedSettingsJsonObject))

        val context = RuntimeEnvironment.application

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

    @Test
    fun checkPrimaryOrganizationSettingsWhenNotSameTest() {

        prepareForSimplifiedResponse()

        val preference1 = UserPreference.General.DefaultReportDuration
        val preference1Name = RuntimeEnvironment.application.getString(preference1.name)
        whenever(userPreferencesManager.name(eq(preference1))).thenReturn(preference1Name)
        whenever(userPreferencesManager.get(eq(preference1))).thenReturn(15)

        organizationManager.checkOrganizationSettingsMatch(organization2).test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(false)
    }

    @Test
    fun checkPrimaryOrganizationSettingsWhenSameTest() {

        prepareForSimplifiedResponse()

        organizationManager.checkOrganizationSettingsMatch(organization2).test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(true)
    }

    @Test
    fun applyPrimaryOrganizationSettingsTest() {

        prepareForSimplifiedResponse()

        val preference1 = UserPreference.General.DefaultReportDuration
        val preference1Name = RuntimeEnvironment.application.getString(preference1.name)
        whenever(userPreferencesManager.name(eq(preference1))).thenReturn(preference1Name)
        whenever(userPreferencesManager.get(eq(preference1))).thenReturn(15)

        organizationManager.applyOrganizationSettings(organization2).test()
            .assertComplete()
            .assertNoErrors()

        verify(userPreferencesManager).set(eq(preference1), any())
        verify(userPreferencesManager).set(eq(preference1), eq(6))
        verify(userPreferencesManager, never()).set(eq(UserPreference.General.DefaultCurrency), any())
        verify(userPreferencesManager, never()).set(eq(UserPreference.General.IncludeCostCenter), any())
        verify(userPreferencesManager, never()).set(eq(UserPreference.Receipts.MinimumReceiptPrice), any())
    }


    companion object {
        @Language("JSON")
        private val settingsJsonObject = JSONObject(
            """
            {
                    "TripDuration": 6,
                    "isocurr": "str",
                    "dateseparator": "-",
                    "trackcostcenter": true,
                    "PredictCats": true,
                    "MatchNameCats": true,
                    "MatchCommentCats": true,
                    "OnlyIncludeExpensable": false,
                    "ExpensableDefault": null,
                    "IncludeTaxField": false,
                    "TaxPercentage": null,
                    "PreTax": false,
                    "EnableAutoCompleteSuggestions": true,
                    "MinReceiptPrice": 5.5,
                    "DefaultToFirstReportDate": null,
                    "ShowReceiptID": false,
                    "UseFullPage": false,
                    "UsePaymentMethods": true,
                    "IncludeCSVHeaders": true,
                    "PrintByIDPhotoKey": false,
                    "PrintCommentByPhoto": false,
                    "EmailTo": "email@to",
                    "EmailCC": "email@cc",
                    "EmailBCC": "email@bcc",
                    "EmailSubject": "email subject",
                    "SaveBW": true,
                    "LayoutIncludeReceiptDate": true,
                    "LayoutIncludeReceiptCategory": true,
                    "LayoutIncludeReceiptPicture": true,
                    "MileageTotalInReport": true,
                    "MileageRate": 10,
                    "MileagePrintTable": false,
                    "MileageAddToPDF": false,
                    "PdfFooterString": "custom footer string"
            }
        """
        )

        @Language("JSON")
        private val simplifiedSettingsJsonObject = JSONObject(
            """
            {
                    "TripDuration": 6,
                    "isocurr": "str",
                    "trackcostcenter": true,
                    "TaxPercentage": null,
                    "MinReceiptPrice": 5.5
            }
        """
        )

    }
}