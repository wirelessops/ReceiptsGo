package co.smartreceipts.android.ocr.widget.tooltip

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import javax.inject.Inject

import co.smartreceipts.android.tooltip.TooltipPresenter
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.widget.tooltip.Tooltip
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable

class OcrInformationalTooltipFragment : Fragment(), TooltipView {

    @Inject
    internal lateinit var tooltipPresenter: TooltipPresenter

    private lateinit var tooltipView: Tooltip

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tooltipView = Tooltip(context)
        return tooltipView
    }

    override fun onStart() {
        super.onStart()
        tooltipPresenter.subscribe()
    }

    override fun onStop() {
        tooltipPresenter.unsubscribe()
        super.onStop()
    }

    override fun getSupportedTooltips(): List<TooltipType> {
        return listOf(TooltipType.OcrInformation)
    }

    override fun display(tooltip: TooltipMetadata) {
        tooltipView.setTooltip(tooltip)
        if (tooltipView.visibility != View.VISIBLE) {
            tooltipView.visibility = View.VISIBLE
        }
    }

    override fun hideTooltip() {
        if (tooltipView.visibility != View.GONE) {
            tooltipView.visibility = View.GONE
        }
    }

    override fun getTooltipClickStream(): Observable<Any> = tooltipView.tooltipClickStream

    override fun getButtonNoClickStream(): Observable<Any> = tooltipView.buttonNoClickStream

    override fun getButtonYesClickStream(): Observable<Any> = tooltipView.buttonYesClickStream

    override fun getButtonCancelClickStream(): Observable<Any> = tooltipView.buttonCancelClickStream

    override fun getCloseIconClickStream(): Observable<Any> = tooltipView.closeIconClickStream
}
