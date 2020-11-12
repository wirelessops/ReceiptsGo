package co.smartreceipts.android.widget.ui

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import co.smartreceipts.android.R
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

        setKeyListenerWithDecimalSeparator()

        this.afterTextChangeEvents()
            .map { it.editable }
            .filter { !it.isNullOrEmpty() }
            .subscribe { editable: Editable? ->
                val correctDecimalSeparator = ModelUtils.decimalSeparator
                val incorrectDecimalSeparator = if (correctDecimalSeparator == '.') ',' else '.'

                val originalText = editable.toString()
                var correctedText = originalText

                var lastSelectionPosition = selectionStart

                val isDecimalSeparatorPresent = originalText.contains('.', true) || originalText.contains(',', true)
                if (isDecimalSeparatorPresent) {
                    setKeyListenerWithoutDecimalSeparator()
                    // due to some specific keyboard behavior (Samsung KitKat users don't have ',') we need to allow user to input any decimal separator
                    // but then correct it if it's not location-based
                    if (originalText.contains(incorrectDecimalSeparator, true)) {
                        correctedText = originalText.replace(incorrectDecimalSeparator, correctDecimalSeparator, true)
                    }
                } else {
                    setKeyListenerWithDecimalSeparator()
                }

                // allowed to input '-' just once at the start of the string, all other '-'s will be removed
                if (originalText.indexOf('-', 1) != -1) {
                    // found extra minus, need to remove it
                    val substring = correctedText.substring(1)
                    val extraMinusesCount = substring.filter { it == '-' }.count()

                    correctedText = correctedText[0] + substring.replace("-", "")
                    lastSelectionPosition -= extraMinusesCount
                }

                // update text and cursor position if needed
                if (correctedText != originalText) {
                    setText(correctedText)
                    setSelection(0.coerceAtLeast(lastSelectionPosition))
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

    private fun setKeyListenerWithDecimalSeparator() {
        keyListener = DigitsKeyListener.getInstance("-0123456789.,")
    }

    private fun setKeyListenerWithoutDecimalSeparator() {
        keyListener = DigitsKeyListener.getInstance("-0123456789")
    }

}