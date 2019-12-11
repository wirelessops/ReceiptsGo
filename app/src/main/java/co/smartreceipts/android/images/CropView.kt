package co.smartreceipts.android.images

import android.graphics.Bitmap
import co.smartreceipts.android.widget.model.UiIndicator
import io.reactivex.Observable
import java.io.File

interface CropView {

    val imageFile: File

    val rotateRightClicks: Observable<Unit>

    val rotateLeftClicks: Observable<Unit>

    val cropToggleClicks: Observable<Unit>


    fun present(indicator: UiIndicator<Bitmap>)

    fun finishCropView(success: Boolean)

    fun getApplyCropClicks(): Observable<Bitmap>

    fun toggleCropMode()
}