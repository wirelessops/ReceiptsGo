package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.widget.viper.BaseViperPresenter
import javax.inject.Inject

@FragmentScope
class AccountPresenter @Inject
constructor(view: AccountView, interactor: AccountInteractor) :
    BaseViperPresenter<AccountView, AccountInteractor>(view, interactor) {

    override fun subscribe() {

        view.present(interactor.getEmail())

        compositeDisposable.add(
            view.logoutButtonClicks
                .map { interactor.logOut() }
                .subscribe({ view.updateProperScreen()}, { t: Throwable? -> view.showError(t?.message?: "Something went wrong") })
        )
    }

}
