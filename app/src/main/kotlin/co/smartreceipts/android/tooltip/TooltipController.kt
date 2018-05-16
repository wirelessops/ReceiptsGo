package co.smartreceipts.android.tooltip

import android.support.annotation.AnyThread
import android.support.annotation.UiThread
import co.smartreceipts.android.tooltip.model.StaticTooltip
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import com.hadisatrio.optional.Optional
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer

/**
 * A contract that defines how we control interactions with a specific tooltip
 */
interface TooltipController {

    /**
     * @return a [Single], which will emit an [Optional] that will contain the appropriate
     * [StaticTooltip] if we should display this item
     */
    @UiThread
    fun shouldDisplayTooltip(): Single<Optional<StaticTooltip>>

    /**
     * @return an [Observable] that will handle and emit a specific type of [TooltipInteraction].
     * This method exists to allow implementers of this contract decide if background thread
     * operations are required before a call is made to [TooltipController.consumeTooltipInteraction]
     */
    @AnyThread
    fun handleTooltipInteraction(interaction: TooltipInteraction): Observable<TooltipInteraction>

    /**
     * @return a [Consumer] that will consume a specific type of [TooltipInteraction] on the Ui Thread
     */
    @UiThread
    fun consumeTooltipInteraction(): Consumer<TooltipInteraction>
}