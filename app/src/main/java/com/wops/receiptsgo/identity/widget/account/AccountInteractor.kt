package com.wops.receiptsgo.identity.widget.account

import androidx.annotation.VisibleForTesting
import com.wops.receiptsgo.identity.apis.organizations.Organization
import com.wops.receiptsgo.identity.apis.organizations.OrganizationModel
import com.wops.receiptsgo.identity.apis.organizations.OrganizationUser
import com.wops.receiptsgo.identity.organization.OrganizationManager
import com.wops.receiptsgo.ocr.purchases.OcrPurchaseTracker
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscription
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscriptionManager
import com.wops.core.di.scopes.ApplicationScope
import com.wops.core.identity.IdentityManager
import com.wops.core.identity.store.EmailAddress
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class AccountInteractor constructor(
    private val identityManager: IdentityManager,
    private val organizationManager: OrganizationManager,
    private val ocrPurchaseTracker: OcrPurchaseTracker,
    private val remoteSubscriptionManager: RemoteSubscriptionManager,
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    @Inject
    constructor(
        identityManager: IdentityManager, organizationManager: OrganizationManager, ocrPurchaseTracker: OcrPurchaseTracker,
        remoteSubscriptionManager: RemoteSubscriptionManager
    ) : this(
        identityManager, organizationManager, ocrPurchaseTracker, remoteSubscriptionManager, Schedulers.io(),
        AndroidSchedulers.mainThread()
    )

    fun logOut() = identityManager.logOut()

    fun getEmail(): EmailAddress = identityManager.email ?: EmailAddress(
        ""
    )


    fun getOrganizations(): Single<List<OrganizationModel>> {

        return organizationManager.getOrganizations()
            .flatMapObservable { Observable.fromIterable(it) }
            .flatMap { organization ->
                organizationManager.checkOrganizationSettingsMatch(organization)
                    .map { settingsMatch -> OrganizationModel(organization, getUserRole(organization), settingsMatch) }
                    .toObservable()
            }
            .toList()
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    fun getOcrRemainingScansStream(): Observable<Int> {
        return ocrPurchaseTracker.remainingScansStream
            .observeOn(observeOnScheduler)
    }

    fun getSubscriptions(): Single<List<RemoteSubscription>> {
        return remoteSubscriptionManager.getRemoteSubscriptions()
            .map { it.toList() }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)

    }

    fun applyOrganizationSettings(organization: Organization): Completable {

        return Single.just(organization)
            .flatMapCompletable { organizationManager.applyOrganizationSettings(it) }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    fun uploadOrganizationSettings(organization: Organization): Completable {

        return Single.just(organization)
            .flatMapCompletable { organizationManager.updateOrganizationSettings(it) }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    @VisibleForTesting
    private fun getUserRole(organization: Organization): OrganizationUser.UserRole {
        val defaultRole = OrganizationUser.UserRole.USER

        for (user in organization.organizationUsers) {
            if (identityManager.userId?.id.equals(user.userId)) {
                 return user.role
            }
        }

        return defaultRole
    }

}