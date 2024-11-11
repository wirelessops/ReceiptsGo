package com.wops.receiptsgo.ocr.widget.configuration

import co.smartreceipts.core.identity.store.EmailAddress
import com.android.billingclient.api.ProductDetails
import io.reactivex.Observable
import io.reactivex.functions.Consumer

interface OcrConfigurationView {

    val logoutButtonClicks: Observable<Unit>

    /**
     *
     * [Observable] that will emit a delayed purchase id if it was saved before
     */
    val delayedPurchaseIdStream: Observable<String>

    /**
     * [Observable] that will emit a value as to whether the user elects enabled or
     * disable OCR
     */
    val ocrIsEnabledCheckboxStream: Observable<Boolean>

    /**
     * [Observable] that will emit a value as to whether the user elects to allows
     * us to save images remotely or not
     */
    val allowUsToSaveImagesRemotelyCheckboxStream: Observable<Boolean>

    /**
     * [Observable] that emit an available purchase whenever a user chooses to
     * initiate a purchase
     */
    val availablePurchaseClicks: Observable<ProductDetails>

    /**
     * [Observable] that emit an available purchase whenever a user clicks on
     * subscriptions
     */
    val subscriptionClicks: Observable<Unit>

    /**
     * [Consumer] for interacting with the user's choice for enabling OCR or not
     */
    val ocrIsEnabledConsumer: Consumer<in Boolean>

    /**
     * [Consumer] for interacting with the user's current select about saving images
     * remotely or not
     */
    val allowUsToSaveImagesRemotelyConsumer: Consumer<in Boolean>

    /**
     * Presents the current user's email address (if any)
     */
    fun present(emailAddress: EmailAddress?)

    /**
     * Presents the current user's remaining scans if logged in
     */
    fun present(remainingScans: Int, isUserLoggedIn: Boolean)

    /**
     * Presents the list of available purchases for this user
     */
    fun present(availablePurchases: List<@JvmSuppressWildcards ProductDetails>)

    /**
     * Saves purchase and navigates user to login screen
     */
    fun delayPurchaseAndPresentNeedToLogin(delayedPurchaseId: String)

    /**
     * navigates user to login screen
     */
    fun navigateToLoginScreen()

    /**
     * navigates user to subscriptions screen
     */
    fun navigateToSubscriptionsScreen()

    /**
     * Toggles the subscriptions option
     */
    fun toggleSubscriptions(isSubscriptionsEnabled: Boolean)
}
