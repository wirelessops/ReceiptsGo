package co.smartreceipts.android.tooltip.privacy

import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.android.utils.TestLazy
import io.reactivex.schedulers.Schedulers
import org.junit.After

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivacyPolicyUserInteractionStoreTest {

    private lateinit var privacyPolicyUserInteractionStore: PrivacyPolicyUserInteractionStore

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())

    private val scheduler = Schedulers.trampoline()

    @Before
    fun setUp() {
        privacyPolicyUserInteractionStore = PrivacyPolicyUserInteractionStore(TestLazy.create(sharedPreferences), scheduler)
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

        val newInstance = PrivacyPolicyUserInteractionStore(TestLazy.create(sharedPreferences), scheduler)
        newInstance.hasUserInteractionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()
    }

}