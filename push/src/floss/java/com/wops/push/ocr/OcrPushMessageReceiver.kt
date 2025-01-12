package com.wops.push.ocr

import com.wops.push.PushMessageReceiver
import io.reactivex.Observable

/**
 * NoOp implementation
 */
class OcrPushMessageReceiver : PushMessageReceiver {

    override fun getPushResponse(): Observable<Any> = Observable.empty()

    override fun onMessageReceived(remoteMessage: Any) {}
}