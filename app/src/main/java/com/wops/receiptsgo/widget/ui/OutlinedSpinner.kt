package com.wops.receiptsgo.widget.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.wops.receiptsgo.R
import com.wops.receiptsgo.databinding.ViewOutlinedSpinnerBinding
import com.wops.receiptsgo.utils.Supplier

class OutlinedSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Supplier<Spinner> {

    private val binding: ViewOutlinedSpinnerBinding = ViewOutlinedSpinnerBinding.inflate(LayoutInflater.from(context), this)

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

            if (captionText != null && captionText.isNotEmpty()) {
                binding.caption.setBackgroundColor(captionBackgroundColor)
                binding.caption.text = captionText
            } else {
                binding.caption.isVisible = false
            }
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

    fun setCaptionText(s: String) {
        binding.caption.text = s
    }
}