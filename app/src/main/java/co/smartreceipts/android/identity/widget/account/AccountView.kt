package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.identity.apis.organizations.OrganizationModel
import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription
import co.smartreceipts.android.widget.model.UiIndicator
import io.reactivex.Observable

interface AccountView {

    val logoutButtonClicks: Observable<Unit>

    val applySettingsClicks: Observable<OrganizationModel>

    val uploadSettingsClicks: Observable<OrganizationModel>


    fun presentEmail(emailAddress: EmailAddress)

    fun presentOrganizations(uiIndicator: UiIndicator<List<OrganizationModel>>)

    fun presentApplyingResult(uiIndicator: UiIndicator<Unit>)

    fun presentUpdatingResult(uiIndicator: UiIndicator<Unit>)

    fun presentOcrScans(remainingScans: Int)

    fun presentSubscriptions(subscriptions: List<RemoteSubscription>)

    fun updateProperScreen()
}
