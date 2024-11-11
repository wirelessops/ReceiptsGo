package co.smartreceipts.push.internal

import io.reactivex.Observable

class FcmTokenRetriever {
    fun getFcmTokenObservable(): Observable<String> = Observable.empty()
}