package co.smartreceipts.android.tooltip

import co.smartreceipts.android.tooltip.model.StaticTooltip
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import io.reactivex.Observable

/**
 * Defines a view contract for displaying tooltips to the end user
 */
interface TooltipView {

    /**
     * @return a [List] of available [StaticTooltip] that can be displayed
     */
    fun getSupportedTooltips(): List<StaticTooltip>

    /**
     * Informs our UI to display a [TooltipMetadata]
     */
    fun display(tooltip: TooltipMetadata)

    /**
     * Informs our UI to hide our [TooltipMetadata] (if one is visible)
     */
    fun hideTooltip()

    /**
     * @return an [Observable] that will emit an object whenever the tooltip view is clicked
     */
    fun getTooltipClickStream(): Observable<Any>

    /**
     * @return an [Observable] that will emit an object whenever the no button is clicked
     */
    fun getButtonNoClickStream(): Observable<Any>

    /**
     * @return an [Observable] that will emit an object whenever the yes button is clicked
     */
    fun getButtonYesClickStream(): Observable<Any>

    /**
     * @return an [Observable] that will emit an object whenever the cancel button is clicked
     */
    fun getButtonCancelClickStream(): Observable<Any>

    /**
     * @return an [Observable] that will emit an object whenever the close icon is clicked
     */
    fun getCloseIconClickStream(): Observable<Any>
}