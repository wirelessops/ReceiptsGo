package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.widget.model.UiIndicator
import io.reactivex.Observable

interface AccountView {

    fun present(emailAddress: EmailAddress)

    fun present(indicator: UiIndicator<Boolean>)

    fun updateProperScreen()

    fun showError(message: String)

    val logoutButtonClicks: Observable<Any>

    val applySettingsButtonClicks: Observable<Any>
}
