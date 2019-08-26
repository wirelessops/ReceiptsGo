package co.smartreceipts.android.receipts.ordering

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import org.junit.After
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReceiptsOrderingMigrationStoreTest {

    private lateinit var migrationStore: ReceiptsOrderingMigrationStore

    private lateinit var preferences: SharedPreferences

    @Mock
    private lateinit var lazy: Lazy<SharedPreferences>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        preferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())

        whenever(lazy.get()).thenReturn(preferences)
        migrationStore = ReceiptsOrderingMigrationStore(lazy)
    }

    @After
    fun tearDown() {
        preferences.edit().clear().apply()
    }

    @Test
    fun getMigrationVersionForNewUsers() {
        migrationStore.getMigrationVersion().test()
                .assertValue(ReceiptsOrderingMigrationStore.MigrationVersion.NotMigrated)
                .assertNoErrors()
                .assertComplete()

        // Confirm that we use 'V2' post migration
        migrationStore.setOrderingMigrationHasOccurred(true)
        migrationStore.getMigrationVersion().test()
                .assertValue(ReceiptsOrderingMigrationStore.MigrationVersion.V2)
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun getMigrationVersionForPreviouslyMigratedV1Users() {
        // Manually mark the v1 version
        preferences.edit().putBoolean("receipt_ordering_migration_has_occurred", true).apply()
        migrationStore.getMigrationVersion().test()
                .assertValue(ReceiptsOrderingMigrationStore.MigrationVersion.V1)
                .assertNoErrors()
                .assertComplete()

        // Confirm that we use 'V2' post migration
        migrationStore.setOrderingMigrationHasOccurred(true)
        migrationStore.getMigrationVersion().test()
                .assertValue(ReceiptsOrderingMigrationStore.MigrationVersion.V2)
                .assertNoErrors()
                .assertComplete()
    }
}