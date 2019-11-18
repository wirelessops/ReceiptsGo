package co.smartreceipts.android.keyboard.decimal

import io.reactivex.Observable


/**
 * A view contract for our special decimal input button for Samsung keyboard users
 */
interface SamsungDecimalInputView {

    /**
     * Informs this view that we should display the decimal input button
     *
     * @param separator the decimal separator String (eg '.' or ',')
     */
    fun showSamsungDecimalInputView(separator: String)

    /**
     * Informs this view that it should be hidden, meaning that the user has a normal keyboard that
     * allows for decimal input
     */
    fun hideSamsungDecimalInputView()

    /**
     * Prompts this view to add a decimal separator string to the actively focused view in this
     * input form
     *
     * @param separator the decimal separator String (eg '.' or ',')
     */
    fun appendDecimalSeparatorToFocusedVied(separator: String)

    /**
     * @return an [Observable] that will emit an [Unit] whenever this view is clicked
     */
    fun getClickStream() : Observable<Unit>
}