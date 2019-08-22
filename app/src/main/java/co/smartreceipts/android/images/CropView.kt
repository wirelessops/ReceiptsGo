package co.smartreceipts.android.images

import android.graphics.Bitmap
import co.smartreceipts.android.widget.model.UiIndicator
import io.reactivex.Observable
import java.io.File

interface CropView {

    val imageFile: File

    val rotateRightClicks: Observable<Any>

    val rotateLeftClicks: Observable<Any>

    val cropToggleClicks: Observable<Any>


    fun present(indicator: UiIndicator<Bitmap>)

    fun finishCropView()

    fun getApplyCropClicks(): Observable<Bitmap>

    fun toggleCropMode()
}