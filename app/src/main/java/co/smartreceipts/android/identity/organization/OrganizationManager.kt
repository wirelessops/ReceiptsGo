package co.smartreceipts.android.identity.organization

import android.text.TextUtils
import co.smartreceipts.android.apis.ApiValidationException
import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.config.ConfigurationManager
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.identity.apis.organizations.Organization
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse
import co.smartreceipts.android.identity.apis.organizations.OrganizationsService
import co.smartreceipts.android.identity.store.MutableIdentityStore
import co.smartreceipts.android.utils.ConfigurableResourceFeature
import co.smartreceipts.android.utils.log.Logger
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

    fun getPrimaryOrganization(): Maybe<Organization> {
        return getOrganizations()
            .filter { !it.organizations.isEmpty() }
            .map { it.organizations[0] }
            .flatMap { organization ->
                if (organization.error.hasError) {
                    Maybe.error<Organization>(ApiValidationException(TextUtils.join(", ", organization.error.errors)))
                } else {
                    Maybe.just(organization)
                }
            }
    }

    private fun getOrganizations(): Maybe<OrganizationsResponse> {
        return if (configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)) {
            getOrganizationsApiRequest()
                .toMaybe()
                .doOnError { throwable -> Logger.error(this, "Failed to complete the organizations request", throwable) }
        } else {
            Maybe.empty()
        }
    }

    private fun getOrganizationsApiRequest(): Single<OrganizationsResponse> {
        return if (identityStore.isLoggedIn) {
            webServiceManager.getService(OrganizationsService::class.java).organizations().lastOrError()
        } else {
            Single.error(IllegalStateException("Cannot fetch the user's organizations until we're logged in"))
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


}
