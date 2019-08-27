package co.smartreceipts.android.identity.organization

import co.smartreceipts.android.apis.ApiValidationException
import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.config.ConfigurationManager
import co.smartreceipts.android.identity.apis.organizations.*
import co.smartreceipts.android.identity.store.MutableIdentityStore
import co.smartreceipts.android.utils.ConfigurableResourceFeature
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OrganizationManagerTest {

    // Class under test
    private lateinit var organizationManager: OrganizationManager

    private val webServiceManager = mock<WebServiceManager>()
    private val identityStore = mock<MutableIdentityStore>()
    private val configurationManager = mock<ConfigurationManager>()
    private val service = mock<OrganizationsService>()
    private val organizationsResponse = mock<OrganizationsResponse>()
    private val appSettingsSynchronizer = mock<AppSettingsSynchronizer>()

    private val organization1 = mock<Organization>()
    private val organization2 = mock<Organization>()
    private val orgError = mock<Error>()
    private val orgSettings = mock<AppSettings>()
    private val appSettings = mock<AppSettings>()
    private val org1Name = "A"
    private val org2Name = "B"

    @Before
    fun setUp() {
        organizationManager = OrganizationManager(
            webServiceManager,
            identityStore,
            configurationManager,
            appSettingsSynchronizer
        )

        whenever(configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)).thenReturn(true)
        whenever(identityStore.isLoggedIn).thenReturn(true)

        whenever(webServiceManager.getService(OrganizationsService::class.java)).thenReturn(service)
        whenever(service.organizations()).thenReturn(Observable.just(organizationsResponse))

        whenever(organizationsResponse.organizations).thenReturn(listOf(organization1))
        whenever(organization1.error).thenReturn(orgError)
        whenever(orgError.hasError).thenReturn(false)
        whenever(organization1.appSettings).thenReturn(orgSettings)
        whenever(organization1.name).thenReturn(org1Name)
        whenever(organization2.name).thenReturn(org2Name)

        whenever(orgSettings.preferences).thenReturn(mock())
        whenever(orgSettings.categories).thenReturn(mock())
        whenever(orgSettings.paymentMethods).thenReturn(mock())
        whenever(orgSettings.csvColumns).thenReturn(mock())
        whenever(orgSettings.pdfColumns).thenReturn(mock())

        whenever(appSettingsSynchronizer.checkCategoriesMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkPaymentMethodsMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkCsvColumnsMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkPdfColumnsMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkOrganizationPreferencesMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.getCurrentAppSettings()).thenReturn(Single.just(appSettings))
    }

    @Test
    fun getOrganizationsTestWithoutOrganizationSyncing() {
        whenever(configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)).thenReturn(false)

        organizationManager.getOrganizations().test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()
    }

    @Test
    fun getOrganizationsTestWithoutLoggingIn() {
        whenever(identityStore.isLoggedIn).thenReturn(false)

        organizationManager.getOrganizations().test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()
    }

    @Test
    fun getOrganizationsTestWithServiceError() {

        whenever(service.organizations()).thenReturn(Observable.error(Exception()))

        organizationManager.getOrganizations().test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(Exception::class.java)
    }

    @Test
    fun getOrganizationsTestWithNoOrganizations() {

        whenever(organizationsResponse.organizations).thenReturn(emptyList())

        organizationManager.getOrganizations().test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()
            .assertResult()
    }

    @Test
    fun getOrganizationsTestWithFewOrganizations() {

        whenever(organizationsResponse.organizations).thenReturn(listOf(organization2, organization1))
        whenever(organization1.error).thenReturn(orgError)
        whenever(organization2.error).thenReturn(orgError)
        whenever(orgError.hasError).thenReturn(false)


        organizationManager.getOrganizations().test()
            .assertComplete()
            .assertNoErrors()
            .assertValueCount(1)
            .assertResult(listOf(organization1, organization2))
    }

    @Test
    fun getPrimaryOrganizationTestWithOneOrganization() {

        organizationManager.getOrganizations().test()
            .assertComplete()
            .assertNoErrors()
            .assertValueCount(1)
            .assertResult(listOf(organization1))
    }

    @Test
    fun getPrimaryOrganizationTestWithOrganizationError() {

        whenever(organizationsResponse.organizations).thenReturn(listOf(organization1))
        whenever(organization1.error).thenReturn(orgError)
        whenever(orgError.hasError).thenReturn(true)

        organizationManager.getOrganizations().test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(ApiValidationException::class.java)
    }

    @Test
    fun checkOrganizationMatchWhenSameTest() {
        whenever(appSettingsSynchronizer.checkOrganizationPreferencesMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkCategoriesMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkPaymentMethodsMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkCsvColumnsMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkPdfColumnsMatch(any())).thenReturn(Single.just(true))

        organizationManager.checkOrganizationSettingsMatch(organization1).test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(true)
    }

    @Test
    fun checkOrganizationMatchWhenNotSameTest() {
        whenever(appSettingsSynchronizer.checkOrganizationPreferencesMatch(any())).thenReturn(Single.just(false))
        whenever(appSettingsSynchronizer.checkCategoriesMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkPaymentMethodsMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkCsvColumnsMatch(any())).thenReturn(Single.just(true))
        whenever(appSettingsSynchronizer.checkPdfColumnsMatch(any())).thenReturn(Single.just(true))

        organizationManager.checkOrganizationSettingsMatch(organization1).test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(false)
    }

    @Test
    fun applyOrganizationSettingsTest() {
        whenever(appSettingsSynchronizer.applyOrganizationPreferences(any())).thenReturn(Completable.complete())
        whenever(appSettingsSynchronizer.applyCategories(any())).thenReturn(Completable.complete())
        whenever(appSettingsSynchronizer.applyPaymentMethods(any())).thenReturn(Completable.complete())
        whenever(appSettingsSynchronizer.applyCsvColumns(any())).thenReturn(Completable.complete())
        whenever(appSettingsSynchronizer.applyPdfColumns(any())).thenReturn(Completable.complete())

        organizationManager.applyOrganizationSettings(organization1).test()
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun applyOrganizationSettingsErrorTest() {
        whenever(appSettingsSynchronizer.applyOrganizationPreferences(any())).thenReturn(Completable.complete())
        whenever(appSettingsSynchronizer.applyCategories(any())).thenReturn(Completable.complete())
        whenever(appSettingsSynchronizer.applyPaymentMethods(any())).thenReturn(Completable.error(java.lang.Exception()))
        whenever(appSettingsSynchronizer.applyCsvColumns(any())).thenReturn(Completable.complete())
        whenever(appSettingsSynchronizer.applyPdfColumns(any())).thenReturn(Completable.complete())

        organizationManager.applyOrganizationSettings(organization1).test()
            .assertNotComplete()
            .assertError(Exception::class.java)
    }

    @Test
    fun updateOrganizationSuccess() {
        whenever(service.updateOrganization(eq(organization1.id), any())).thenReturn(Observable.just(organizationsResponse))

        organizationManager.updateOrganizationSettings(organization1).test()
            .assertComplete()
            .assertNoErrors()

    }

    @Test
    fun updateOrganizationError() {
        whenever(service.updateOrganization(eq(organization1.id), any())).thenReturn(Observable.error(java.lang.Exception()))

        organizationManager.updateOrganizationSettings(organization1).test()
            .assertNotComplete()
            .assertError(Exception::class.java)
    }


}