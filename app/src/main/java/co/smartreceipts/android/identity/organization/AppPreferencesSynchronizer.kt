package co.smartreceipts.android.identity.organization

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.identity.apis.organizations.AppSettings
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.log.Logger
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@ApplicationScope
class AppPreferencesSynchronizer @Inject constructor(private val userPreferenceManager: UserPreferenceManager) {

    /**
     * @param organizationPreferences Organization app preferences
     * @return Single that emits {true} if organization app preferences match the app preferences, else emits {false}
     */
    internal fun checkOrganizationPreferencesMatch(organizationPreferences: AppSettings.OrganizationPreferences): Single<Boolean> {
        return Single.just(organizationPreferences)
            .flatMap { settings ->
                userPreferenceManager.userPreferencesSingle
                    .flatMapObservable<UserPreference<*>> { Observable.fromIterable(it) }
                    .map { userPreference -> checkPreferenceMatch(settings, userPreference) }
                    .filter { it.isPresent }
                    .map<Boolean> { it.get() }
                    .contains(false)
            }
            .map { someSettingsDontMatch -> !someSettingsDontMatch }
    }

    internal fun applyOrganizationPreferences(organizationSettings: AppSettings.OrganizationPreferences): Completable {
        return Single.just(organizationSettings)
            .flatMapCompletable { settings ->
                userPreferenceManager.userPreferencesSingle
                    .flatMapObservable<UserPreference<*>> { Observable.fromIterable(it) }
                    .map { userPreference -> applyPreference(settings, userPreference) }
                    .ignoreElements()
            }

    }

    internal fun <T> checkPreferenceMatch(
        settings: AppSettings.OrganizationPreferences,
        toPreference: UserPreference<T>
    ): Optional<Boolean> {
        return checkJsonPreferenceAndApply(settings.preferencesJson, toPreference, false)
    }

    internal fun <T> applyPreference(settings: AppSettings.OrganizationPreferences, toPreference: UserPreference<T>): Optional<Boolean> {
        return checkJsonPreferenceAndApply(settings.preferencesJson, toPreference, true)
    }

    /**
     * @param preferencesJson     the organization preferences
     * @param toPreference preference to check
     * @param apply        boolean value that indicates if organization preference value must be applied to app preferences
     * @param <T>          the type of the preference value
     * @return {true} if app preferences already contain same preference value, else {false},
     * Optional.absent if preference was not found
     **/
    @Throws(Exception::class)
    private fun <T> checkJsonPreferenceAndApply(
        preferencesJson: JSONObject,
        toPreference: UserPreference<T>,
        apply: Boolean
    ): Optional<Boolean> {

        val preferenceName = userPreferenceManager.name(toPreference)

        if (!preferencesJson.has(preferenceName)) {
            Logger.warn(this, "Failed to find preference: {}", preferenceName)
            return Optional.absent()
        }

        if (preferencesJson.isNull(preferenceName)) {
            Logger.debug(this, "Skipping preference \'{}\', which is defined as null.", preferenceName)
            return Optional.absent()
        }

        try {
            val preferenceValue: T = when {
                java.lang.Boolean::class.java == toPreference.type -> java.lang.Boolean.valueOf(preferencesJson.getBoolean(preferenceName)) as T
                java.lang.String::class.java == toPreference.type -> preferencesJson.getString(preferenceName) as T
                java.lang.Float::class.java == toPreference.type -> java.lang.Float.valueOf(preferencesJson.getString(preferenceName)) as T
                java.lang.Integer::class.java == toPreference.type -> java.lang.Integer.valueOf(preferencesJson.getInt(preferenceName)) as T
                else -> throw Exception("Unsupported organization setting type ${toPreference.type} for $preferenceName")
            }

            val appPreferenceValue = userPreferenceManager[toPreference]
            Logger.debug(this, "Checking organization preference \'{}\'. app: '{}', organization: \'{}\'", preferenceName,
                appPreferenceValue, preferenceValue)

            val equals = appPreferenceValue == preferenceValue
            if (!equals && apply) {
                Logger.debug(this, "Applying organization preferences: set \'{}\' to \'{}\'", preferenceName, preferenceValue)
                userPreferenceManager[toPreference] = preferenceValue
            }

            return Optional.of(equals)

        } catch (e: JSONException) {
            Logger.error(this, e)
            throw Exception("Unsupported organization setting type for $preferenceName")
        }

    }
}