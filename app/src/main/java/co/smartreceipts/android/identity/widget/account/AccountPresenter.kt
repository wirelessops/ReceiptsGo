package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.identity.apis.organizations.OrganizationModel
import co.smartreceipts.android.utils.log.Logger
import co.smartreceipts.android.widget.model.UiIndicator
import co.smartreceipts.android.widget.viper.BaseViperPresenter
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
            interactor.getSubscriptionsStream()
                .subscribe(view::presentSubscriptions)
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
