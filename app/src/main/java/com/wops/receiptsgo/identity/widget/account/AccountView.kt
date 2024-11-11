package com.wops.receiptsgo.identity.widget.account

import com.wops.receiptsgo.identity.apis.organizations.OrganizationModel
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscription
import com.wops.receiptsgo.widget.model.UiIndicator
import co.smartreceipts.core.identity.store.EmailAddress
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
