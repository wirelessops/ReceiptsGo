package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.identity.apis.organizations.Organization
import co.smartreceipts.android.widget.model.UiIndicator
import co.smartreceipts.android.widget.viper.BaseViperPresenter
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@FragmentScope
class AccountPresenter @Inject constructor(view: AccountView, interactor: AccountInteractor) :
    BaseViperPresenter<AccountView, AccountInteractor>(view, interactor) {

    private val organizationSubject = BehaviorSubject.create<Organization>()

    override fun subscribe() {

        view.presentEmail(interactor.getEmail())

        compositeDisposable.add(
            view.logoutButtonClicks
                .doOnNext { interactor.logOut() }
                .subscribe { view.updateProperScreen() }
        )

        compositeDisposable.add(
            interactor.getOrganization()
                .subscribe { organizationIndicator ->
                        view.presentOrganization(organizationIndicator)

                        if (organizationIndicator.state == UiIndicator.State.Success && organizationIndicator.data.isPresent) {
                            organizationSubject.onNext(organizationIndicator.data.get().organization)
                        }
                }
        )

        compositeDisposable.add(
            view.applySettingsClicks
                .flatMap {
                    organizationSubject
                        .flatMap(interactor::applyOrganizationSettings)
                }
                .subscribe { indicator -> view.presentApplyingResult(indicator) }
        )


        compositeDisposable.add(interactor.getOcrRemainingScansStream()
            .subscribe(view::presentOcrScans)
        )

    }

}
