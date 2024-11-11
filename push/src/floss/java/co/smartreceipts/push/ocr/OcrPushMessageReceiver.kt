package co.smartreceipts.push.ocr

import co.smartreceipts.push.PushMessageReceiver
import io.reactivex.Observable

/**
 * NoOp implementation
 */
class OcrPushMessageReceiver : PushMessageReceiver {

    override fun getPushResponse(): Observable<Any> = Observable.empty()

    override fun onMessageReceived(remoteMessage: Any) {}
}