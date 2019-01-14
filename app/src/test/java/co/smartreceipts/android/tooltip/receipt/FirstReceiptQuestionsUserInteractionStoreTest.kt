package co.smartreceipts.android.tooltip.receipt

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
class FirstReceiptQuestionsUserInteractionStoreTest {

    private lateinit var firstReceiptQuestionsUserInteractionStore: FirstReceiptQuestionsUserInteractionStore

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)

    private val scheduler = Schedulers.trampoline()

    @Before
    fun setUp() {
        firstReceiptQuestionsUserInteractionStore = FirstReceiptQuestionsUserInteractionStore(TestLazy.create(sharedPreferences), scheduler)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().apply()
    }

    @Test
    fun hasUserInteractionWithTaxesQuestionOccurredDefaultsToFalse() {
        firstReceiptQuestionsUserInteractionStore.hasUserInteractionWithTaxesQuestionOccurred()
                .test()
                .await()
                .assertValue(false)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun setUserHasInteractedWithTaxesQuestion() {
        firstReceiptQuestionsUserInteractionStore.setInteractionWithTaxesQuestionHasOccurred(true)
        firstReceiptQuestionsUserInteractionStore.hasUserInteractionWithTaxesQuestionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()

        val newInstance = FirstReceiptQuestionsUserInteractionStore(TestLazy.create(sharedPreferences), scheduler)
        newInstance.hasUserInteractionWithTaxesQuestionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun hasUserInteractionWithPaymentMethodsQuestionOccurredDefaultsToFalse() {
        firstReceiptQuestionsUserInteractionStore.hasUserInteractionWithPaymentMethodsQuestionOccurred()
                .test()
                .await()
                .assertValue(false)
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun setUserHasInteractedWithPaymentMethodsQuestion() {
        firstReceiptQuestionsUserInteractionStore.setInteractionWithPaymentMethodsQuestionHasOccurred(true)
        firstReceiptQuestionsUserInteractionStore.hasUserInteractionWithPaymentMethodsQuestionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()

        val newInstance = FirstReceiptQuestionsUserInteractionStore(TestLazy.create(sharedPreferences), scheduler)
        newInstance.hasUserInteractionWithPaymentMethodsQuestionOccurred()
                .test()
                .await()
                .assertValue(true)
                .assertComplete()
                .assertNoErrors()
    }
}