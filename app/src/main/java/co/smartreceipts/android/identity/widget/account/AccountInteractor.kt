package co.smartreceipts.android.identity.widget.account

import android.content.Context
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.identity.IdentityManager
import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.widget.model.UiIndicator
import io.reactivex.subjects.ReplaySubject
import javax.inject.Inject

@ApplicationScope
class AccountInteractor @Inject constructor(private val context: Context, private val identityManager: IdentityManager) {

    private var uiIndicatorReplaySubject: ReplaySubject<UiIndicator<Boolean>>? = null

    @Synchronized
    fun logOut() = identityManager.logOut()


    @Synchronized
    fun getEmail(): EmailAddress = identityManager.email ?: EmailAddress("")
}