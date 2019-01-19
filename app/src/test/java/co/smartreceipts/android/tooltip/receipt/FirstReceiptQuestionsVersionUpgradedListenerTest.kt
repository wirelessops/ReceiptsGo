package co.smartreceipts.android.tooltip.receipt

import co.smartreceipts.android.tooltip.receipt.FirstReceiptQuestionsUserInteractionStore
import co.smartreceipts.android.tooltip.receipt.FirstReceiptQuestionsVersionUpgradedListener
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirstReceiptQuestionsVersionUpgradedListenerTest {

    private lateinit var listener: FirstReceiptQuestionsVersionUpgradedListener

    @Mock
    private lateinit var store: FirstReceiptQuestionsUserInteractionStore

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        listener = FirstReceiptQuestionsVersionUpgradedListener(store)
    }

    @Test
    fun onVersionUpgradeFromExistingVersionInformsTheStoreThatAnInteractionHasOccurred() {
        listener.onVersionUpgrade(100, 200)
        verify(store).setInteractionWithTaxesQuestionHasOccurred(true)
        verify(store).setInteractionWithPaymentMethodsQuestionHasOccurred(true)
    }

    @Test
    fun onVersionUpgradeFromFreshInstallDoesNothing() {
        listener.onVersionUpgrade(-1, 200)
        verifyZeroInteractions(store)
    }
}