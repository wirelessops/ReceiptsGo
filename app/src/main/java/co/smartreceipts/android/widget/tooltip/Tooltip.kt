package co.smartreceipts.android.widget.tooltip

import android.content.Context
import android.content.res.ColorStateList
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.AppTooltipBinding
import co.smartreceipts.android.tooltip.model.TooltipDisplayStyle
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class Tooltip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    val tooltipClickStream: Observable<Unit>
        get() = _tooltipClicks

    val buttonNoClickStream: Observable<Unit>
        get() = _buttonNoClicks

    val buttonYesClickStream: Observable<Unit>
        get() = _buttonYesClicks

    val buttonCancelClickStream: Observable<Unit>
        get() = _buttonCancelClicks

    val closeIconClickStream: Observable<Unit>
        get() = _closeIconClicks

    private val _tooltipClicks = PublishSubject.create<Unit>()
    private val _buttonNoClicks = PublishSubject.create<Unit>()
    private val _buttonYesClicks = PublishSubject.create<Unit>()
    private val _buttonCancelClicks = PublishSubject.create<Unit>()
    private val _closeIconClicks = PublishSubject.create<Unit>()


    private val binding: AppTooltipBinding = AppTooltipBinding.inflate(LayoutInflater.from(context), this, true)


    fun setTooltip(tooltip: TooltipMetadata) {
        // Initially hide the "yes/no" question buttons
        binding.no.isVisible = false
        binding.yes.isVisible = false

        when (tooltip.displayStyle) {
            TooltipDisplayStyle.Question -> {
                // Display these again if and only if a question
                binding.no.isVisible = true
                binding.yes.isVisible = true
                setInfoBackground()
            }
            TooltipDisplayStyle.Informational -> setInfoBackground()
            TooltipDisplayStyle.Error -> setErrorBackground()
        }

        // All tooltips must have a message (otherwise there's no reason to show them)
        binding.message.text = tooltip.message

        binding.errorIcon.isVisible = tooltip.showWarningIcon

        // Tooltips may either have a close icon (ie 'X') or cancel button (but not both)
        binding.cancel.isVisible = tooltip.showCancelButton && !tooltip.showCloseIcon
        binding.closeIcon.isVisible = tooltip.showCloseIcon && !tooltip.showCancelButton

        // Configure all click streams
        setOnClickListener { _tooltipClicks.onNext(Unit) }
        binding.no.setOnClickListener { _buttonNoClicks.onNext(Unit) }
        binding.yes.setOnClickListener { _buttonYesClicks.onNext(Unit) }
        binding.cancel.setOnClickListener { _buttonCancelClicks.onNext(Unit) }
        binding.closeIcon.setOnClickListener { _closeIconClicks.onNext(Unit) }
    }

    fun setError(@StringRes messageStringId: Int, closeClickListener: OnClickListener?) {
        setViewStateError()
        binding.message.text = context.getText(messageStringId)
        showCloseIcon(closeClickListener)
    }

    fun setErrorWithoutClose(@StringRes messageStringId: Int, tooltipClickListener: OnClickListener?) {
        setViewStateError()
        binding.closeIcon.visibility = GONE
        binding.message.text = context.getText(messageStringId)
        setTooltipClickListener(tooltipClickListener)
    }

    fun setInfoWithCloseIcon(
        @StringRes infoStringId: Int, tooltipClickListener: OnClickListener?,
        closeClickListener: OnClickListener?, vararg formatArgs: Any?
    ) {
        setInfoMessage(context.getString(infoStringId, *formatArgs))
        setTooltipClickListener(tooltipClickListener)
        showCloseIcon(closeClickListener)
        binding.errorIcon.visibility = VISIBLE
        binding.no.visibility = GONE
        binding.yes.visibility = GONE
        binding.cancel.visibility = GONE
    }

    fun setInfo(@StringRes infoStringId: Int, tooltipClickListener: OnClickListener?, closeClickListener: OnClickListener?) {
        setInfoMessage(infoStringId)
        setTooltipClickListener(tooltipClickListener)
        showCloseIcon(closeClickListener)
        binding.errorIcon.visibility = GONE
        binding.no.visibility = GONE
        binding.yes.visibility = GONE
        binding.cancel.visibility = GONE
    }

    fun setQuestion(@StringRes questionStringId: Int, noClickListener: OnClickListener?, yesClickListener: OnClickListener?) {
        setInfoMessage(questionStringId)
        binding.no.visibility = VISIBLE
        binding.yes.visibility = VISIBLE
        binding.cancel.visibility = GONE
        binding.closeIcon.visibility = GONE
        binding.errorIcon.visibility = GONE

        binding.no.setOnClickListener(noClickListener)
        binding.yes.setOnClickListener(yesClickListener)
    }

    fun setInfoMessage(@StringRes messageStringId: Int) {
        setInfoBackground()
        binding.message.setText(messageStringId)
    }

    fun setInfoMessage(text: CharSequence?) {
        setInfoBackground()
        binding.message.text = text
    }

    fun setTooltipClickListener(tooltipClickListener: OnClickListener?) {
        setOnClickListener(tooltipClickListener)
    }

    fun showCloseIcon(closeClickListener: OnClickListener?) {
        binding.closeIcon.visibility = VISIBLE
        binding.closeIcon.setOnClickListener(closeClickListener)
    }

    fun showCancelButton(cancelClickListener: OnClickListener) {
        binding.cancel.visibility = VISIBLE
        binding.cancel.setOnClickListener(cancelClickListener)
    }

    private fun setErrorBackground() {
        binding.container.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.smart_receipts_colorError)
        )
    }

    private fun setInfoBackground() {
        binding.container.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.tooltip_bg))
    }

    private fun setViewStateError() {
        setErrorBackground()
        binding.closeIcon.visibility = VISIBLE
        binding.errorIcon.visibility = VISIBLE
        binding.no.visibility = GONE
        binding.yes.visibility = GONE
        binding.cancel.visibility = GONE
    }

    fun hideWithAnimation() {
        if (visibility != GONE) {
            TransitionManager.beginDelayedTransition(parent as ViewGroup, AutoTransition())
            visibility = GONE
        }
    }

    fun showWithAnimation() {
        if (visibility != VISIBLE) {
            TransitionManager.beginDelayedTransition(parent as ViewGroup, AutoTransition())
            visibility = VISIBLE
        }
    }
}