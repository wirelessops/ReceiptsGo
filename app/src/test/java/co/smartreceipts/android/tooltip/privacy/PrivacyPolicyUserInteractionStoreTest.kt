package co.smartreceipts.android.tooltip.privacy

import android.preference.PreferenceManager
import org.junit.After

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PrivacyPolicyUserInteractionStoreTest {

    lateinit var privacyPolicyUserInteractionStore: PrivacyPolicyUserInteractionStore

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)

    @Before
    fun setUp() {
        privacyPolicyUserInteractionStore = PrivacyPolicyUserInteractionStore(sharedPreferences)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().apply()
    }

    @Test
    fun hasUserInteractionOccurredDefaultsToFalse() {
        privacyPolicyUserInteractionStore.hasUserInteractionOccurred()
                .test()
                .await()
                .assertValue(false)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun setUserHasInteractedWithPrivacyPolicy() {
        privacyPolicyUserInteractionStore.setUserHasInteractedWithPrivacyPolicy(true)
        privacyPolicyUserInteractionStore.hasUserInteractionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()

        val newInstance = PrivacyPolicyUserInteractionStore(sharedPreferences)
        newInstance.hasUserInteractionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()
    }

}