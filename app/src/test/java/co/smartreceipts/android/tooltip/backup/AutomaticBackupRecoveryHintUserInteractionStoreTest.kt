package co.smartreceipts.android.tooltip.backup

import android.preference.PreferenceManager
import co.smartreceipts.android.utils.TestLazy
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AutomaticBackupRecoveryHintUserInteractionStoreTest {

    private lateinit var automaticBackupRecoveryHintUserInteractionStore: AutomaticBackupRecoveryHintUserInteractionStore

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)

    private val scheduler = Schedulers.trampoline()

    @Before
    fun setUp() {
        automaticBackupRecoveryHintUserInteractionStore = AutomaticBackupRecoveryHintUserInteractionStore(TestLazy.create(sharedPreferences), scheduler)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().apply()
    }

    @Test
    fun hasUserInteractionOccurredDefaultsToFalse() {
        automaticBackupRecoveryHintUserInteractionStore.hasUserInteractionOccurred()
                .test()
                .await()
                .assertValue(false)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun setUserHasInteractedWithPrivacyPolicy() {
        automaticBackupRecoveryHintUserInteractionStore.setUserHasInteractedWithAutomaticBackupRecoveryHint(true)
        automaticBackupRecoveryHintUserInteractionStore.hasUserInteractionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()

        val newInstance = AutomaticBackupRecoveryHintUserInteractionStore(TestLazy.create(sharedPreferences), scheduler)
        newInstance.hasUserInteractionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()
    }

}