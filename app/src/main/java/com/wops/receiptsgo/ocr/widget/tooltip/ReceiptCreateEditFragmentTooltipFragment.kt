package com.wops.receiptsgo.ocr.widget.tooltip

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.wops.receiptsgo.tooltip.TooltipPresenter
import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.wops.receiptsgo.widget.tooltip.Tooltip
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Acts as a dedicated fragment for showing a tooltip when editing/creating a receipt.
 */
class ReceiptCreateEditFragmentTooltipFragment : Fragment(), TooltipView {

    @Inject
    internal lateinit var tooltipPresenter: TooltipPresenter

    private lateinit var tooltipView: Tooltip

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tooltipView = Tooltip(requireContext())
        tooltipView.isVisible = false
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
        return listOf(
            TooltipType.FirstReceiptUseTaxesQuestion, TooltipType.FirstReceiptUsePaymentMethodsQuestion, TooltipType.OcrInformation,
            TooltipType.ImageCropping, TooltipType.ConfigureSecondTaxHint
        )
    }

    override fun display(tooltip: TooltipMetadata) {
        tooltipView.setTooltip(tooltip)
        if (!tooltipView.isVisible) {
            tooltipView.isVisible = true
        }
    }

    override fun hideTooltip() {
        if (tooltipView.isVisible) {
            tooltipView.isVisible = false
        }
    }

    override fun getTooltipClickStream(): Observable<Unit> = tooltipView.tooltipClickStream

    override fun getButtonNoClickStream(): Observable<Unit> = tooltipView.buttonNoClickStream

    override fun getButtonYesClickStream(): Observable<Unit> = tooltipView.buttonYesClickStream

    override fun getButtonCancelClickStream(): Observable<Unit> = tooltipView.buttonCancelClickStream

    override fun getCloseIconClickStream(): Observable<Unit> = tooltipView.closeIconClickStream
}
