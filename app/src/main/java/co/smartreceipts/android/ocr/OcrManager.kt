package co.smartreceipts.android.ocr

import co.smartreceipts.android.ocr.apis.model.OcrResponse
import co.smartreceipts.android.ocr.widget.alert.OcrProcessingStatus
import io.reactivex.Observable
import java.io.File

interface OcrManager {

    fun initialize()

    fun scan(file: File): Observable<OcrResponse>

    fun getOcrProcessingStatus(): Observable<OcrProcessingStatus>
}