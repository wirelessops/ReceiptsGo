package com.wops.receiptsgo.tooltip

import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import io.reactivex.Observable

/**
 * Defines a view contract for displaying tooltips to the end user
 */
interface TooltipView {

    /**
     * @return a [List] of available [TooltipType] that can be displayed
     */
    fun getSupportedTooltips(): List<TooltipType>

    /**
     * Informs our UI to display a [TooltipMetadata]
     */
    fun display(tooltip: TooltipMetadata)

    /**
     * Informs our UI to hide our [TooltipMetadata] (if one is visible)
     */
    fun hideTooltip()

    /**
     * @return an [Observable] that will emit Unit whenever the tooltip view is clicked
     */
    fun getTooltipClickStream(): Observable<Unit>

    /**
     * @return an [Observable] that will emit Unit whenever the no button is clicked
     */
    fun getButtonNoClickStream(): Observable<Unit>

    /**
     * @return an [Observable] that will emit Unit whenever the yes button is clicked
     */
    fun getButtonYesClickStream(): Observable<Unit>

    /**
     * @return an [Observable] that will emit Unit whenever the cancel button is clicked
     */
    fun getButtonCancelClickStream(): Observable<Unit>

    /**
     * @return an [Observable] that will emit Unit whenever the close icon is clicked
     */
    fun getCloseIconClickStream(): Observable<Unit>
}