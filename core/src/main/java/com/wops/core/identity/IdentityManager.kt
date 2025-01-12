package com.wops.core.identity

import com.wops.core.identity.apis.me.MeResponse
import com.wops.core.identity.apis.push.UpdatePushTokensRequest
import com.wops.core.identity.store.IdentityStore
import io.reactivex.Observable

interface IdentityManager : IdentityStore {

    val isLoggedInStream: Observable<Boolean>

    fun initialize()

    fun getMe(): Observable<MeResponse>

    fun  updateMe(request: UpdatePushTokensRequest): Observable<MeResponse>
}