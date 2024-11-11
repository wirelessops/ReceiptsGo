package com.wops.receiptsgo.ocr.widget.configuration

import co.smartreceipts.analytics.Analytics
import com.wops.receiptsgo.config.ConfigurationManager
import com.wops.receiptsgo.ocr.purchases.OcrPurchaseTracker
import com.wops.receiptsgo.purchases.PurchaseManager
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.source.PurchaseSource
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import co.smartreceipts.core.identity.IdentityManager
import co.smartreceipts.core.identity.store.EmailAddress
import com.android.billingclient.api.ProductDetails
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OcrConfigurationInteractorTest {
    private val identityManager = Mockito.mock(IdentityManager::class.java)
    private val ocrPurchaseTracker = Mockito.mock(OcrPurchaseTracker::class.java)
    private val purchaseManager = Mockito.mock(PurchaseManager::class.java)
    private val userPreferenceManager = Mockito.mock(UserPreferenceManager::class.java)
    private val configurationManager = Mockito.mock(ConfigurationManager::class.java)
    private val analytics = Mockito.mock(Analytics::class.java)
    private val availablePurchaseSkuDetails = Mockito.mock(ProductDetails::class.java)
    private val availablePurchaseSkuDetailsOneTimePurchaseOfferDetails =
        mock<ProductDetails.OneTimePurchaseOfferDetails>()
    private val availablePurchaseSkuDetails2 = Mockito.mock(ProductDetails::class.java)
    private val availablePurchaseSkuDetails2OneTimePurchaseOfferDetails =
        mock<ProductDetails.OneTimePurchaseOfferDetails>()
    private val interactor = OcrConfigurationInteractor(
        identityManager,
        ocrPurchaseTracker,
        purchaseManager,
        userPreferenceManager,
        configurationManager,
        analytics,
        Schedulers.trampoline()
    )

    @Test
    fun getEmail() {
        val emailAddress = EmailAddress("email")
        whenever(identityManager.email).thenReturn(emailAddress)
        Assert.assertEquals(emailAddress, interactor.email)
    }

    @Test
    fun getRemainingScansStream() {
        val scanSubject = PublishSubject.create<Int>()
        whenever(ocrPurchaseTracker.remainingScansStream).thenReturn(scanSubject)
        val testObserver = interactor.getRemainingScansStream().test()
        scanSubject.onNext(61)
        testObserver.assertValue(61)
        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
    }

    @Test
    fun getAvailableOcrPurchasesOrdersByPrice() {
        setupPurchases(InAppPurchase.OcrScans50.sku, InAppPurchase.OcrScans10.sku)

        val testObserver = interactor.getAvailableOcrPurchases().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertValue(listOf(availablePurchaseSkuDetails2, availablePurchaseSkuDetails))
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    fun getAvailableOcrPurchasesIgnoresSubscriptions() {
        setupPurchases(InAppPurchase.OcrScans50.sku, InAppPurchase.SmartReceiptsPlus.sku)

        val testObserver = interactor.getAvailableOcrPurchases().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertValue(listOf(availablePurchaseSkuDetails))
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    fun getAvailableOcrPurchasesIgnoresNonOcrOnes() {
        setupPurchases(InAppPurchase.OcrScans50.sku, InAppPurchase.TestConsumablePurchase.sku)

        val testObserver = interactor.getAvailableOcrPurchases().test()
        testObserver.awaitTerminalEvent()
        testObserver.assertValue(listOf(availablePurchaseSkuDetails))
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    fun startOcrPurchase() {
        whenever(availablePurchaseSkuDetails.productId).thenReturn(InAppPurchase.OcrScans50.sku)
        interactor.startOcrPurchase(availablePurchaseSkuDetails)
        Mockito.verify(purchaseManager).initiatePurchase(availablePurchaseSkuDetails, PurchaseSource.Ocr)
    }

    @Test
    fun getOcrIsEnabled() {
        whenever(userPreferenceManager.getObservable(UserPreference.Misc.OcrIsEnabled))
            .thenReturn(Observable.just(false))
        val testObserver1 = interactor.getOcrIsEnabled().test()
        testObserver1.awaitTerminalEvent()
        testObserver1.assertValue(false)
        testObserver1.assertComplete()
        testObserver1.assertNoErrors()
        whenever(userPreferenceManager.getObservable(UserPreference.Misc.OcrIsEnabled))
            .thenReturn(Observable.just(true))
        val testObserver2 = interactor.getOcrIsEnabled().test()
        testObserver2.awaitTerminalEvent()
        testObserver2.assertValue(true)
        testObserver2.assertComplete()
        testObserver2.assertNoErrors()
    }

    @Test
    fun setOcrIsEnabled() {
        interactor.setOcrIsEnabled(false)
        Mockito.verify(userPreferenceManager)[UserPreference.Misc.OcrIsEnabled] = false
        interactor.setOcrIsEnabled(true)
        Mockito.verify(userPreferenceManager)[UserPreference.Misc.OcrIsEnabled] = true
    }

    @Test
    fun getAllowUsToSaveImagesRemotely() {
        whenever(userPreferenceManager.getObservable(UserPreference.Misc.OcrIncognitoMode))
            .thenReturn(Observable.just(true))
        val testObserver1 = interactor.getAllowUsToSaveImagesRemotely().test()
        testObserver1.awaitTerminalEvent()
        testObserver1.assertValue(false)
        testObserver1.assertComplete()
        testObserver1.assertNoErrors()
        whenever(userPreferenceManager.getObservable(UserPreference.Misc.OcrIncognitoMode))
            .thenReturn(Observable.just(false))
        val testObserver2 = interactor.getAllowUsToSaveImagesRemotely().test()
        testObserver2.awaitTerminalEvent()
        testObserver2.assertValue(true)
        testObserver2.assertComplete()
        testObserver2.assertNoErrors()
    }

    @Test
    fun setAllowUsToSaveImagesRemotely() {
        interactor.setAllowUsToSaveImagesRemotely(false)
        Mockito.verify(userPreferenceManager)[UserPreference.Misc.OcrIncognitoMode] = true
        interactor.setAllowUsToSaveImagesRemotely(true)
        Mockito.verify(userPreferenceManager)[UserPreference.Misc.OcrIncognitoMode] = false
    }

    private fun setupPurchases(purchase1Sku: String, purchase2Sku: String) {
        whenever(availablePurchaseSkuDetails.productId).thenReturn(purchase1Sku)
        whenever(availablePurchaseSkuDetailsOneTimePurchaseOfferDetails.priceAmountMicros).thenReturn(500000L)
        whenever(availablePurchaseSkuDetails.oneTimePurchaseOfferDetails).thenReturn(
            availablePurchaseSkuDetailsOneTimePurchaseOfferDetails
        )

        whenever(availablePurchaseSkuDetails2.productId).thenReturn(purchase2Sku)
        whenever(availablePurchaseSkuDetails2OneTimePurchaseOfferDetails.priceAmountMicros).thenReturn(100000L)
        whenever(availablePurchaseSkuDetails2.oneTimePurchaseOfferDetails).thenReturn(
            availablePurchaseSkuDetails2OneTimePurchaseOfferDetails
        )

        val purchaseSet: Set<ProductDetails> =
            HashSet(listOf(availablePurchaseSkuDetails, availablePurchaseSkuDetails2))
        whenever(purchaseManager.allAvailablePurchaseSkus).thenReturn(Single.just(purchaseSet))
    }
}