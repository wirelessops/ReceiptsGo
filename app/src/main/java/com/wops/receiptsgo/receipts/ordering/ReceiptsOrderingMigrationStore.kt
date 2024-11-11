package com.wops.receiptsgo.receipts.ordering

import android.content.SharedPreferences
import com.wops.core.di.scopes.ApplicationScope
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

/**
 * Maintains the responsibility for tracking if we've ever previously migrated from our legacy
 * custom order ids to the modern set
 */
@ApplicationScope
class ReceiptsOrderingMigrationStore @Inject constructor(private val preferences: Lazy<SharedPreferences>) {

    /**
     * @return a [Single] that will emit the current [MigrationVersion]
     */
    fun getMigrationVersion(): Single<MigrationVersion> {
        return Single.fromCallable {
                    val v3MigrationHasOccurred = preferences.get().getBoolean(V3_ORDERING_KEY, false)
                    val v2MigrationHasOccurred = preferences.get().getBoolean(V2_ORDERING_KEY, false)
                    val v1MigrationHasOccurred = preferences.get().getBoolean(V1_ORDERING_KEY, false)
                    return@fromCallable when {
                        v3MigrationHasOccurred -> MigrationVersion.V3
                        v2MigrationHasOccurred -> MigrationVersion.V2
                        v1MigrationHasOccurred -> MigrationVersion.V1
                        else -> MigrationVersion.NotMigrated
                    }
                }
    }

    /**
     * Indicates that the migration has successfully occurred. Since both [MigrationVersion.V1] and
     * [MigrationVersion.V2] take us to an [ReceiptsOrderer.OrderingType.Ordered] state, we set both
     * to true when this method is called
     */
    fun setOrderingMigrationHasOccurred(hasOccurred: Boolean) {
        preferences.get().edit().putBoolean(V1_ORDERING_KEY, hasOccurred).apply()
        preferences.get().edit().putBoolean(V2_ORDERING_KEY, hasOccurred).apply()
        preferences.get().edit().putBoolean(V3_ORDERING_KEY, hasOccurred).apply()
    }

    /**
     * Note we have four defined migrations.
     */
    enum class MigrationVersion {
        /**
         * Indicates that no migration ever occurred (e.g. new app installs, never upgraded, etc)
         */
        NotMigrated,

        /**
         * The first version of the migration. This takes from [ReceiptsOrderer.OrderingType.None] or
         * [ReceiptsOrderer.OrderingType.Legacy] to [ReceiptsOrderer.OrderingType.Ordered]
         */
        V1,

        /**
         * The second version of the migration. This takes from [ReceiptsOrderer.OrderingType.PartiallyOrdered]
         * to [ReceiptsOrderer.OrderingType.Ordered]
         */
        V2,

        /**
         * The third version of the migration. This keeps receipts in the [ReceiptsOrderer.OrderingType.Ordered]
         * format and removes the ability to drag receipts in the list
         */
        V3
    }

    companion object {
        private const val V1_ORDERING_KEY = "receipt_ordering_migration_has_occurred"
        private const val V2_ORDERING_KEY = "receipt_ordering_migration_has_occurred_v2_b" // _b for test build users
        private const val V3_ORDERING_KEY = "receipt_ordering_migration_has_occurred_v3_b" // _b for test build users
    }
}
