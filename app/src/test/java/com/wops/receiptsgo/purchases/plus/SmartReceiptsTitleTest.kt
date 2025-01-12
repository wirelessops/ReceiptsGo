package com.wops.receiptsgo.purchases.plus

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wops.receiptsgo.R
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import wb.android.flex.Flex

@RunWith(RobolectricTestRunner::class)
class SmartReceiptsTitleTest {

    private lateinit var smartReceiptsTitle: SmartReceiptsTitle

    @Mock
    private lateinit var flex: Flex

    @Mock
    private lateinit var purchaseWallet: PurchaseWallet

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        smartReceiptsTitle = SmartReceiptsTitle(ApplicationProvider.getApplicationContext(), flex, purchaseWallet)

        val plusString = ApplicationProvider.getApplicationContext<Context>().getString(R.string.sr_app_name_plus)
        val freeString = ApplicationProvider.getApplicationContext<Context>().getString(R.string.sr_app_name)
        whenever(flex.getString(ApplicationProvider.getApplicationContext(), R.string.sr_app_name_plus)).thenReturn(plusString)
        whenever(flex.getString(ApplicationProvider.getApplicationContext(), R.string.sr_app_name)).thenReturn(freeString)
    }

    @Test
    fun getWithPlusSubscription() {
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true)
        val plusString = ApplicationProvider.getApplicationContext<Context>().getString(R.string.sr_app_name_plus)
        assertEquals(plusString, smartReceiptsTitle.get())
    }

    @Test
    fun getWithoutPlusSubscription() {
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(false)
        val freeString = ApplicationProvider.getApplicationContext<Context>().getString(R.string.sr_app_name)
        assertEquals(freeString, smartReceiptsTitle.get())
    }
}