package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.identity.IdentityManager
import co.smartreceipts.android.identity.OrganizationManager
import co.smartreceipts.android.identity.apis.organizations.Organization
import co.smartreceipts.android.identity.apis.organizations.OrganizationUser
import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.widget.model.UiIndicator
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
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    @Inject
    constructor(identityManager: IdentityManager, organizationManager: OrganizationManager) :
            this(identityManager, organizationManager, Schedulers.io(), AndroidSchedulers.mainThread())


    fun logOut() = identityManager.logOut()

    fun getEmail(): EmailAddress = identityManager.email ?: EmailAddress("")


    fun getOrganization(): Observable<UiIndicator<OrganizationModel>> {

        return Observable.concat(Observable.just(UiIndicator.loading<OrganizationModel>()),
            organizationManager.primaryOrganization
                .flatMapSingle { organization: Organization ->
                    organizationManager.checkOrganizationSettingsMatch(organization)
                        .flatMap { settingsMatch: Boolean ->
                            Single.just(OrganizationModel(organization, getUserRole(organization), settingsMatch))
                        }
                }
                .map { model: OrganizationModel -> UiIndicator.success(model) }
                .flatMapObservable { indicator: UiIndicator<OrganizationModel> ->
                    Observable.just(indicator)
                }
        )
            .onErrorReturn { t: Throwable ->
                when (t) {
                    is NoSuchElementException -> UiIndicator.idle()
                    else -> UiIndicator.error()
                }
            }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    private fun getUserRole(organization: Organization): OrganizationUser.UserRole {
        // TODO: 12.11.2018 implement getting real user role
        return OrganizationUser.UserRole.USER
    }

    fun applyOrganizationSettings(organization: Organization): Observable<UiIndicator<Unit>> {

        return Single.just(organization)
            .flatMap<UiIndicator<Unit>> { org: Organization ->
                organizationManager.applyOrganizationSettings(org)
                    .andThen(Single.just(UiIndicator.success()))
            }
            .onErrorReturn { UiIndicator.error() }
            .toObservable()
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }


    data class OrganizationModel(
        val organization: Organization,
        val userRole: OrganizationUser.UserRole,
        val settingsMatch: Boolean
    )

}