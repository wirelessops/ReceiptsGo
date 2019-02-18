package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription
import co.smartreceipts.android.widget.model.UiIndicator
import io.reactivex.Observable

interface AccountView {

    val logoutButtonClicks: Observable<Any>

    val applySettingsClicks: Observable<Any>

    fun presentEmail(emailAddress: EmailAddress)

    fun presentOrganization(uiIndicator: UiIndicator<AccountInteractor.OrganizationModel>)

    fun presentApplyingResult(uiIndicator: UiIndicator<Unit>)

    fun presentOcrScans(remainingScans: Int)

    fun presentSubscriptions(subscriptions: List<RemoteSubscription>)

    fun updateProperScreen()
}
