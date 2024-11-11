package com.wops.receiptsgo.tooltip.report

import android.content.SharedPreferences
import com.wops.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.utils.rx.RxSchedulers
import dagger.Lazy
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

@ApplicationScope
class FirstReportHintUserInteractionStore @Inject constructor(private val preferences: Lazy<SharedPreferences>,
                                                              @Named(RxSchedulers.IO) private val scheduler: Scheduler) {

    fun hasUserInteractionOccurred(): Single<Boolean> {
        return Single.fromCallable { preferences.get().getBoolean(KEY, false) }
                .subscribeOn(scheduler)
    }

    fun setInteractionHasOccurred(userInteractedWithThis: Boolean) {
        preferences.get().edit().putBoolean(KEY, userInteractedWithThis).apply()
    }

    companion object {
        private const val KEY = "user_interacted_with_first_hint"
    }
}
