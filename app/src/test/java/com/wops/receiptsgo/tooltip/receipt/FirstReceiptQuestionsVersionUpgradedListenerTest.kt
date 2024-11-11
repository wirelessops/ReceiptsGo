package com.wops.receiptsgo.tooltip.receipt

import com.wops.receiptsgo.tooltip.receipt.FirstReceiptQuestionsUserInteractionStore
import com.wops.receiptsgo.tooltip.receipt.FirstReceiptQuestionsVersionUpgradedListener
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
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