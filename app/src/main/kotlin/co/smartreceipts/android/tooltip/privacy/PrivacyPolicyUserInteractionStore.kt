package co.smartreceipts.android.tooltip.privacy

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import co.smartreceipts.android.di.scopes.ApplicationScope
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class PrivacyPolicyUserInteractionStore(private val preferences: SharedPreferences) {

    @Inject
    constructor(context: Context) : this(PreferenceManager.getDefaultSharedPreferences(context))

    fun hasUserInteractionOccurred(): Single<Boolean> {
        return Single.fromCallable { preferences.getBoolean(KEY, false) }
                .subscribeOn(Schedulers.io())
    }

    fun setUserHasInteractedWithPrivacyPolicy(hasUserInteractedWithPrivacyPolicy: Boolean) {
        preferences.edit().putBoolean(KEY, hasUserInteractedWithPrivacyPolicy).apply()
    }

    companion object {
        private val KEY = "user_click_privacy_prompt"
    }
}
