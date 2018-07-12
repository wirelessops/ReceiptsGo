package co.smartreceipts.android.purchases.plus

import co.smartreceipts.android.R
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
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
        smartReceiptsTitle = SmartReceiptsTitle(RuntimeEnvironment.application, flex, purchaseWallet)

        val plusString = RuntimeEnvironment.application.getString(R.string.sr_app_name_plus)
        val freeString = RuntimeEnvironment.application.getString(R.string.sr_app_name)
        whenever(flex.getString(RuntimeEnvironment.application, R.string.sr_app_name_plus)).thenReturn(plusString)
        whenever(flex.getString(RuntimeEnvironment.application, R.string.sr_app_name)).thenReturn(freeString)
    }

    @Test
    fun getWithPlusSubscription() {
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true)
        val plusString = RuntimeEnvironment.application.getString(R.string.sr_app_name_plus)
        assertEquals(plusString, smartReceiptsTitle.get())
    }

    @Test
    fun getWithoutPlusSubscription() {
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(false)
        val freeString = RuntimeEnvironment.application.getString(R.string.sr_app_name)
        assertEquals(freeString, smartReceiptsTitle.get())
    }
}