package co.smartreceipts.android.tooltip.privacy

import android.content.SharedPreferences
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.utils.rx.RxSchedulers
import dagger.Lazy
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

@ApplicationScope
class PrivacyPolicyUserInteractionStore @Inject constructor(private val preferences: Lazy<SharedPreferences>,
                                                            @Named(RxSchedulers.IO) private val scheduler: Scheduler) {

    fun hasUserInteractionOccurred(): Single<Boolean> {
        return Single.fromCallable { preferences.get().getBoolean(KEY, false) }
                .subscribeOn(scheduler)
    }

    fun setUserHasInteractedWithPrivacyPolicy(hasUserInteractedWithPrivacyPolicy: Boolean) {
        preferences.get().edit().putBoolean(KEY, hasUserInteractedWithPrivacyPolicy).apply()
    }

    companion object {
        private const val KEY = "user_click_privacy_prompt"
    }
}
