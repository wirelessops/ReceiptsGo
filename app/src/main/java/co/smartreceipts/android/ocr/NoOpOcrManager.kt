package co.smartreceipts.android.ocr

import co.smartreceipts.android.ocr.apis.model.OcrResponse
import co.smartreceipts.android.ocr.widget.alert.OcrProcessingStatus
import co.smartreceipts.core.di.scopes.ApplicationScope
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject

@ApplicationScope
class  NoOpOcrManager @Inject constructor(): OcrManager {

    override fun initialize() {}

    override fun scan(file: File): Observable<OcrResponse> = Observable.just( OcrResponse())

    override fun getOcrProcessingStatus(): Observable<OcrProcessingStatus> = Observable.empty()
}