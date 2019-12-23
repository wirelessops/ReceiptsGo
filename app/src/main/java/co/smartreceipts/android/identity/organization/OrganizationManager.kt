package co.smartreceipts.android.identity.organization

import android.text.TextUtils
import co.smartreceipts.android.apis.ApiValidationException
import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.config.ConfigurationManager
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.identity.apis.organizations.*
import co.smartreceipts.core.identity.store.MutableIdentityStore
import co.smartreceipts.android.utils.ConfigurableResourceFeature
import co.smartreceipts.core.utils.log.Logger
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.Function5
import javax.inject.Inject


@ApplicationScope
class OrganizationManager @Inject constructor(
    private val webServiceManager: WebServiceManager,
    private val identityStore: MutableIdentityStore,

    private val configurationManager: ConfigurationManager,
    private val appSettingsSynchronizer: AppSettingsSynchronizer
) {

    fun getOrganizations(): Maybe<List<Organization>> {
        return getOrganizationsResponse()
            .filter { it.organizations.isNotEmpty() }
            .map { it.organizations }
            .flatMap {
                for (organization in it) {
                    if (organization.error.hasError) {
                        return@flatMap Maybe.error<List<Organization>>(ApiValidationException(TextUtils.join(", ", organization.error.errors)))
                    }
                }
                return@flatMap Maybe.just(it)
            }
            .map { it.sortedBy { organization -> organization.name } }

    }

    private fun getOrganizationsResponse(): Maybe<OrganizationsResponse> {
        return if (identityStore.isLoggedIn && configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)) {
            webServiceManager.getService(OrganizationsService::class.java).organizations().lastOrError()
                .toMaybe()
                .doOnError { throwable -> Logger.error(this, "Failed to complete the organizations request", throwable) }
        } else {
            Maybe.empty()
        }
    }

    /**
     * @return Single that emits {true} if all organization settings (preferences, columns, categories and payment methods) match app preferences, else emits {false}
     */
    fun checkOrganizationSettingsMatch(organization: Organization): Single<Boolean> {

        val organizationSettings = organization.appSettings

        return Single.zip(
            appSettingsSynchronizer.checkOrganizationPreferencesMatch(organizationSettings.preferences),
            appSettingsSynchronizer.checkCategoriesMatch(organizationSettings.categories),
            appSettingsSynchronizer.checkPaymentMethodsMatch(organizationSettings.paymentMethods),
            appSettingsSynchronizer.checkCsvColumnsMatch(organizationSettings.csvColumns),
            appSettingsSynchronizer.checkPdfColumnsMatch(organizationSettings.pdfColumns),
            Function5<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean>
            { settingsMatch, categoriesMatch, paymentMethodsMatch, csvMatch, pdfMatch ->
                settingsMatch && categoriesMatch && paymentMethodsMatch && csvMatch && pdfMatch
            })
    }


    /**
     * @return Completable that completes if all organization settings (preferences, columns, categories and payment methods) were applied successfully
     */
    fun applyOrganizationSettings(organization: Organization): Completable {

        val organizationSettings = organization.appSettings

        return Completable.concatArray(
            appSettingsSynchronizer.applyOrganizationPreferences(organizationSettings.preferences),
            appSettingsSynchronizer.applyCategories(organizationSettings.categories),
            appSettingsSynchronizer.applyPaymentMethods(organizationSettings.paymentMethods),
            appSettingsSynchronizer.applyCsvColumns(organizationSettings.csvColumns),
            appSettingsSynchronizer.applyPdfColumns(organizationSettings.pdfColumns)
        )
    }

    fun updateOrganizationSettings(organization: Organization): Completable {
        return updateOrganizationApiRequest(organization)
            .ignoreElement()
    }

    private fun updateOrganizationApiRequest(organization: Organization): Single<OrganizationsResponse> {
        return if (identityStore.isLoggedIn) {
            val service = webServiceManager.getService(OrganizationsService::class.java)

            appSettingsSynchronizer.getCurrentAppSettings()
                .flatMapObservable { settings: AppSettings -> service.updateOrganization(organization.id, AppSettingsPutWrapper(settings)) }
                .lastOrError()
        } else {
            Single.error(IllegalStateException("Cannot update organizations until user is logged in"))
        }
    }


}
