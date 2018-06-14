package co.smartreceipts.android.receipts.ordering

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import co.smartreceipts.android.di.scopes.ApplicationScope
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Maintains the responsibility for tracking if we've ever previously migrated from our legacy
 * custom order ids to the modern set
 */
@ApplicationScope
class ReceiptsOrderingMigrationStore(private val preferences: SharedPreferences) {

    @Inject
    constructor(context: Context) : this(PreferenceManager.getDefaultSharedPreferences(context))

    fun hasOrderingMigrationOccurred(): Single<Boolean> {
        return Single.fromCallable {
                    preferences.getBoolean(KEY, false)
                }
                .subscribeOn(Schedulers.io())
    }

    fun setOrderingMigrationOccurred(hasOccurred: Boolean) {
        preferences.edit().putBoolean(KEY, hasOccurred).apply()
    }

    companion object {
        private val KEY = "receipt_ordering_migration_has_occurred"
    }
}
