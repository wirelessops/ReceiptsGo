package co.smartreceipts.android.widget.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ViewSpinnerBinding
import co.smartreceipts.android.utils.Supplier

class OutlinedSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Supplier<Spinner> {

    private val binding: ViewSpinnerBinding = ViewSpinnerBinding.inflate(LayoutInflater.from(context), this)

    init {
        context.withStyledAttributes(attrs, R.styleable.OutlinedSpinner) {
            val captionText = getString(R.styleable.OutlinedSpinner_captionText)
            val captionBackgroundColor =
                getColor(R.styleable.OutlinedSpinner_captionBackgroundColor, ContextCompat.getColor(context, R.color.background))
            val startIcon = getDrawable(R.styleable.OutlinedSpinner_startIcon)

            if (startIcon != null) {
                binding.startIcon.isVisible = true
                binding.startIcon.setImageDrawable(startIcon)
            } else {
                binding.startIcon.isVisible = false
            }

            binding.caption.setBackgroundColor(captionBackgroundColor)
            binding.caption.text = captionText
        }

    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        binding.spinner.setOnTouchListener(l)
    }

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        binding.spinner.onFocusChangeListener = l
    }

    override fun get(): Spinner {
        return binding.spinner
    }

    fun getSelectedItem(): Any? {
        return binding.spinner.selectedItem
    }

    fun setAdapter(adapter: SpinnerAdapter) {
        binding.spinner.adapter = adapter
    }

    fun setSelection(position: Int) {
        binding.spinner.setSelection(position)
    }
}