package co.smartreceipts.android.widget.ui

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import co.smartreceipts.analytics.log.Logger.debug
import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.utils.ModelUtils
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents

/**
 * EditText that is supposed to present Price value with defined [decimalPlaces].
 * It allows to input digits and locale-based decimal separator.
 * Text formatting is performed after call [formatPriceText] method.
 */
class PriceInputEditText @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attributeSet, defStyleAttr) {

    private var decimalPlaces: Int = Price.TOTAL_DECIMAL_PRECISION

    init {
        inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED

        imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI

        keyListener = DigitsKeyListener.getInstance("0123456789${ModelUtils.decimalSeparator}")

        this.afterTextChangeEvents()
            .map { it.editable }
            .filter { !it.isNullOrEmpty() }
            .subscribe {editable: Editable? ->
                // don't allow to input more decimal separators than one (this is not handled by Android for ',')
                val text = editable.toString()
                val decimalSeparator = ModelUtils.decimalSeparator
                if (text.indexOf(decimalSeparator) != text.lastIndexOf(decimalSeparator)) {
                    setText(text.dropLast(text.length - text.lastIndexOf(decimalSeparator)))
                    setSelection(length())
                }
            }
    }

    fun setDecimalPlaces(places: Int) {
        if (decimalPlaces != places) {
            decimalPlaces = places
            formatPriceText()
        }
    }


    fun formatPriceText() {
        val text = text.toString()

        val tryParse = ModelUtils.tryParse(text)
        val formatted = ModelUtils.getDecimalFormattedValue(tryParse, decimalPlaces)

        setText(formatted)
        setSelection(formatted.length)
    }


}