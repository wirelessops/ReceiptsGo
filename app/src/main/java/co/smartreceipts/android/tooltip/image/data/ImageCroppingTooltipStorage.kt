package co.smartreceipts.android.tooltip.image.data

import io.reactivex.Single

interface ImageCroppingTooltipStorage {

    fun getCroppingScreenWasShown(): Single<Boolean>

    fun getCroppingTooltipWasHandled(): Single<Boolean>

    fun setCroppingScreenWasShown(value: Boolean)

    fun setCroppingTooltipWasHandled(value: Boolean)
}