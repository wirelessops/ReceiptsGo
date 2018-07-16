package co.smartreceipts.android.receipts.ordering

import android.content.SharedPreferences
import co.smartreceipts.android.di.scopes.ApplicationScope
import dagger.Lazy
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Maintains the responsibility for tracking if we've ever previously migrated from our legacy
 * custom order ids to the modern set
 */
@ApplicationScope
class ReceiptsOrderingMigrationStore @Inject constructor(private val preferences: Lazy<SharedPreferences>) {

    fun hasOrderingMigrationOccurred(): Single<Boolean> {
        return Single.fromCallable {
                    preferences.get().getBoolean(KEY, false)
                }
                .subscribeOn(Schedulers.io())
    }

    fun setOrderingMigrationOccurred(hasOccurred: Boolean) {
        preferences.get().edit().putBoolean(KEY, hasOccurred).apply()
    }

    companion object {
        private val KEY = "receipt_ordering_migration_has_occurred"
    }
}
