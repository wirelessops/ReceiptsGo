package co.smartreceipts.core.identity

import co.smartreceipts.core.identity.apis.me.MeResponse
import co.smartreceipts.core.identity.store.IdentityStore
import io.reactivex.Observable

interface IdentityManagerInterface : IdentityStore {

    fun getMe(): Observable<MeResponse>

    val isLoggedInStream: Observable<Boolean>
}