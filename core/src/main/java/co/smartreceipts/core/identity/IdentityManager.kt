package co.smartreceipts.core.identity

import co.smartreceipts.core.identity.apis.me.MeResponse
import co.smartreceipts.core.identity.apis.push.UpdatePushTokensRequest
import co.smartreceipts.core.identity.store.IdentityStore
import io.reactivex.Observable

interface IdentityManager : IdentityStore {

    val isLoggedInStream: Observable<Boolean>

    fun initialize()

    fun getMe(): Observable<MeResponse>

    fun  updateMe(request: UpdatePushTokensRequest): Observable<MeResponse>
}