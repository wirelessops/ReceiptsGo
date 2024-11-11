package com.wops.receiptsgo.identity.widget.account

import co.smartreceipts.analytics.log.Logger
import com.wops.receiptsgo.identity.apis.organizations.OrganizationModel
import com.wops.receiptsgo.widget.model.UiIndicator
import com.wops.receiptsgo.widget.viper.BaseViperPresenter
import co.smartreceipts.core.di.scopes.FragmentScope
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@FragmentScope
class AccountPresenter @Inject constructor(view: AccountView, interactor: AccountInteractor) :
    BaseViperPresenter<AccountView, AccountInteractor>(view, interactor) {

    private val organizationsSubject = PublishSubject.create<UiIndicator<List<OrganizationModel>>>()

    override fun subscribe() {

        view.presentEmail(interactor.getEmail())

        compositeDisposable.add(
            view.logoutButtonClicks
                .doOnNext { interactor.logOut() }
                .subscribe { view.updateProperScreen() }
        )

        compositeDisposable.add(
            organizationsSubject
                .subscribe {
                    view.presentOrganizations(it)
                    Logger.debug(this, "Updating organizations list")
                })

        compositeDisposable.add(
            Observable.concat(
                Observable.just(UiIndicator.loading()),
                requireOrganizations()
            )
                .subscribe { organizationsSubject.onNext(it) }
        )

        compositeDisposable.add(
            view.applySettingsClicks
                .flatMap<UiIndicator<Unit>> { organizationModel ->
                    Logger.debug(this, "Applying organization settings: {}", organizationModel.organization.name)
                    interactor.applyOrganizationSettings(organizationModel.organization)
                        .andThen(Observable.just(UiIndicator.success()))
                }
                .doAfterNext { requireOrganizations().subscribe { organizationsSubject.onNext(it) } }
                .onErrorReturn { UiIndicator.error() }
                .subscribe(view::presentApplyingResult)
        )

        compositeDisposable.add(
            view.uploadSettingsClicks
                .flatMap<UiIndicator<Unit>> { organizationModel ->
                    Logger.debug(this, "Updating organization settings: {}", organizationModel.organization.name)
                    interactor.uploadOrganizationSettings(organizationModel.organization)
                        .andThen(Observable.just(UiIndicator.success()))
                }
                .doAfterNext { requireOrganizations().subscribe { organizationsSubject.onNext(it) } }
                .onErrorReturn { UiIndicator.error() }
                .subscribe(view::presentUpdatingResult)
        )


        compositeDisposable.add(
            interactor.getOcrRemainingScansStream()
                .subscribe(view::presentOcrScans)
        )

        compositeDisposable.add(
            interactor.getSubscriptions()
                .subscribe { list ->
                    when {
                        list.isNotEmpty() -> view.presentSubscriptions(list)
                    }
                }
        )
    }


    private fun requireOrganizations(): Observable<UiIndicator<List<OrganizationModel>>> {
        return interactor.getOrganizations()
            .toObservable()
            .map { if (it.isNotEmpty()) UiIndicator.success(it) else UiIndicator.idle() }
            .onErrorReturn { throwable ->
                when (throwable) {
                    is NoSuchElementException -> UiIndicator.idle()
                    else -> UiIndicator.error()
                }
            }

    }
}
