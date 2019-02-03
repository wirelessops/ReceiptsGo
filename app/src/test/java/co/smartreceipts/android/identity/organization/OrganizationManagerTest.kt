package co.smartreceipts.android.identity.organization

import co.smartreceipts.android.apis.ApiValidationException
import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.config.ConfigurationManager
import co.smartreceipts.android.identity.apis.organizations.*
import co.smartreceipts.android.identity.store.MutableIdentityStore
import co.smartreceipts.android.utils.ConfigurableResourceFeature
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
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


}