package com.wops.receiptsgo.identity.organization

import com.wops.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.analytics.log.Logger
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONException
import javax.inject.Inject

@ApplicationScope
class AppPreferencesSynchronizer @Inject constructor(private val userPreferenceManager: UserPreferenceManager) {


    /**
     * @return Single that emits JSONObject with all app's preferences
     */
    internal fun getAppPreferences(): Single<Map<String, Any>> {
        val appPreferencesMap: MutableMap<String, Any> = mutableMapOf()

        return userPreferenceManager.userPreferencesSingle
            .flatMap { userPreferences ->
                for (userPreference in userPreferences) {
                    appPreferencesMap[userPreferenceManager.name(userPreference)] = userPreferenceManager.get(userPreference)
                }
                Single.just(appPreferencesMap)
            }
    }

    /**
     * @param organizationPreferences Organization app preferences
     * @return Single that emits {true} if organization app preferences match the app preferences, else emits {false}
     */
    internal fun checkOrganizationPreferencesMatch(organizationPreferences: Map<String, Any?>): Single<Boolean> {
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

    internal fun applyOrganizationPreferences(organizationSettings: Map<String, Any?>): Completable {
        return Single.just(organizationSettings)
            .flatMapCompletable { settings ->
                userPreferenceManager.userPreferencesSingle
                    .flatMapObservable<UserPreference<*>> { Observable.fromIterable(it) }
                    .map { userPreference -> applyPreference(settings, userPreference) }
                    .ignoreElements()
            }

    }

    internal fun <T> checkPreferenceMatch(
        settings: Map<String, Any?>,
        toPreference: UserPreference<T>
    ): Optional<Boolean> {
        return checkJsonPreferenceAndApply(settings, toPreference, false)
    }

    internal fun <T> applyPreference(settings: Map<String, Any?>, toPreference: UserPreference<T>): Optional<Boolean> {
        return checkJsonPreferenceAndApply(settings, toPreference, true)
    }

    /**
     * @param prefsMap     the organization preferences
     * @param toPreference preference to check
     * @param apply        boolean value that indicates if organization preference value must be applied to app preferences
     * @param <T>          the type of the preference value
     * @return {true} if app preferences already contain same preference value, else {false},
     * Optional.absent if preference was not found
     **/
    @Throws(Exception::class)
    private fun <T> checkJsonPreferenceAndApply(
        prefsMap: Map<String, Any?>,
        toPreference: UserPreference<T>,
        apply: Boolean
    ): Optional<Boolean> {

        val preferenceName = userPreferenceManager.name(toPreference)

        if (!prefsMap.containsKey(preferenceName)) {
            Logger.warn(this, "Failed to find preference: {}", preferenceName)
            return Optional.absent()
        }

        if (prefsMap[preferenceName] == null) {
            Logger.debug(this, "Skipping preference \'{}\', which is defined as null.", preferenceName)
            return Optional.absent()
        }

        try {

            val preferenceValue: T = when {
                java.lang.Boolean::class.java == toPreference.type -> java.lang.Boolean.valueOf(prefsMap[preferenceName].toString()) as T
                java.lang.String::class.java == toPreference.type -> prefsMap[preferenceName].toString() as T
                java.lang.Float::class.java == toPreference.type -> java.lang.Float.valueOf(prefsMap[preferenceName].toString()) as T
                java.lang.Integer::class.java == toPreference.type -> java.lang.Integer.valueOf(prefsMap[preferenceName].toString()) as T
                else -> throw Exception("Unsupported organization setting type ${toPreference.type} for $preferenceName")
            }

            val appPreferenceValue = userPreferenceManager[toPreference]
            Logger.debug(
                this, "Checking organization preference \'{}\'. app: '{}', organization: \'{}\'", preferenceName,
                appPreferenceValue, preferenceValue
            )

            val equals: Boolean = appPreferenceValue == preferenceValue

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