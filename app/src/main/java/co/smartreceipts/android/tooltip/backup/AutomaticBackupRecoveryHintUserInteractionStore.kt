package co.smartreceipts.android.tooltip.backup

import android.content.SharedPreferences
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.utils.rx.RxSchedulers
import dagger.Lazy
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

@ApplicationScope
class AutomaticBackupRecoveryHintUserInteractionStore @Inject constructor(private val preferences: Lazy<SharedPreferences>,
                                                                          @Named(RxSchedulers.IO) private val scheduler: Scheduler) {

    fun hasUserInteractionOccurred(): Single<Boolean> {
        return Single.fromCallable { preferences.get().getBoolean(KEY, false) }
                .subscribeOn(scheduler)
    }

    fun setUserHasInteractedWithAutomaticBackupRecoveryHint(userInteractedWithThis: Boolean) {
        preferences.get().edit().putBoolean(KEY, userInteractedWithThis).apply()
    }

    companion object {
        private const val KEY = "user_clicked_automatic_backup_recovery_hint"
    }
}
