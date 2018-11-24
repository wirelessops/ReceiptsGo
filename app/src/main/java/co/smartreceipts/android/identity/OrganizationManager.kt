package co.smartreceipts.android.identity

import android.support.annotation.VisibleForTesting
import android.text.TextUtils
import co.smartreceipts.android.apis.ApiValidationException
import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.config.ConfigurationManager
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.identity.apis.organizations.AppSettings.OrganizationSettings
import co.smartreceipts.android.identity.apis.organizations.Organization
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse
import co.smartreceipts.android.identity.apis.organizations.OrganizationsService
import co.smartreceipts.android.identity.store.MutableIdentityStore
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.ConfigurableResourceFeature
import co.smartreceipts.android.utils.log.Logger
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONException
import javax.inject.Inject

@ApplicationScope
class OrganizationManager @Inject constructor(
    private val webServiceManager: WebServiceManager,
    private val identityStore: MutableIdentityStore,
    private val userPreferenceManager: UserPreferenceManager,
    private val configurationManager: ConfigurationManager
) {

    // TODO: 06.11.2018 add applying Categories, PaymentMethods and Columns


    fun getPrimaryOrganization(): Maybe<Organization> {
        return getOrganizations()
            .filter { !it.organizations.isEmpty() }
            .map { it.organizations[0] }
            .flatMap { organization ->
                if (organization.error.hasError) {
                    return@flatMap Maybe.error<Organization>(ApiValidationException(TextUtils.join(", ", organization.error.errors)))
                } else {
                    return@flatMap Maybe.just(organization)
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


    fun applyOrganizationSettings(organization: Organization): Completable {

        return Observable.just(organization.appSettings.settings)
            .flatMapCompletable { organizationSettings ->
                userPreferenceManager.userPreferencesSingle
                    .flatMapObservable<UserPreference<*>> { Observable.fromIterable(it) }
                    .map { userPreference -> checkPreferenceMatch(organizationSettings, userPreference, true) }
                    .ignoreElements()
            }

    }

    /**
     * @return Single that emits {true} if organization settings match app settings, else emits {false}
     */
    fun checkOrganizationSettingsMatch(organization: Organization): Single<Boolean> {

        return Single.just(organization.appSettings.settings)
            .flatMap { organizationSettings ->
                userPreferenceManager.userPreferencesSingle
                    .flatMapObservable<UserPreference<*>> { Observable.fromIterable(it) }
                    .map { userPreference -> checkPreferenceMatch(organizationSettings, userPreference, false) }
                    .filter { it.isPresent }
                    .map<Boolean> { it.get() }
                    .contains(false)
            }
            .map { someSettingsDontMach -> !someSettingsDontMach }

    }

    /**
     * @param settings     the organization settings
     * @param toPreference preference to check
     * @param apply        boolean value that indicates if organization preference value must be applied to app settings
     * @param <T>          the type of the preference value
     * @return Observable that emits {true} if app settings already contains same preference value, else emits {false}
    </T> */
    @VisibleForTesting
    @Throws(Exception::class)
    internal fun <T> checkPreferenceMatch(
        settings: OrganizationSettings,
        toPreference: UserPreference<T>,
        apply: Boolean
    ): Optional<Boolean> {

        val preferenceName = userPreferenceManager.name(toPreference)
        val settingsObject = settings.jsonObject

        if (!settingsObject.has(preferenceName)) {
            Logger.warn(this, "Failed to find preference: {}", preferenceName)
            return Optional.absent()
        }

        if (settingsObject.isNull(preferenceName)) {
            Logger.debug(this, "Skipping preference \'{}\', which is defined as null.", preferenceName)
            return Optional.absent()
        }

        try {
            val preferenceValue: T = when {
                java.lang.Boolean::class.java == toPreference.type -> java.lang.Boolean.valueOf(settingsObject.getBoolean(preferenceName)) as T
                java.lang.String::class.java == toPreference.type -> settingsObject.getString(preferenceName) as T
                java.lang.Float::class.java == toPreference.type -> java.lang.Float.valueOf(settingsObject.getString(preferenceName)) as T
                java.lang.Integer::class.java == toPreference.type -> java.lang.Integer.valueOf(settingsObject.getInt(preferenceName)) as T
                else -> throw Exception("Unsupported organization setting type ${toPreference.type} for $preferenceName")
            }

            Logger.debug(this, "Checking organization settings: app: \'{}\', organization: \'{}\'", preferenceName, preferenceValue)

            val equals = userPreferenceManager[toPreference] == preferenceValue
            if (!equals && apply) {
                Logger.debug(this, "Applying organization settings: set \'{}\' to \'{}\'", preferenceName, preferenceValue)
                userPreferenceManager[toPreference] = preferenceValue
            }

            return Optional.of(equals)

        } catch (e: JSONException) {
            Logger.error(this, e)
            throw Exception("Unsupported organization setting type for $preferenceName")
        }

    }

}
