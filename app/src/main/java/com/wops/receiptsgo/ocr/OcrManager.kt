package com.wops.receiptsgo.ocr

import com.wops.receiptsgo.ocr.apis.model.OcrResponse
import com.wops.receiptsgo.ocr.widget.alert.OcrProcessingStatus
import io.reactivex.Observable
import java.io.File

interface OcrManager {

    fun initialize()

    fun scan(file: File): Observable<OcrResponse>

    fun getOcrProcessingStatus(): Observable<OcrProcessingStatus>
}