package com.wops.receiptsgo.autocomplete

import androidx.annotation.UiThread
import com.wops.receiptsgo.model.AutoCompleteUpdateEvent
import io.reactivex.Observable


/**
 * Defines a contract that can be used to display a list of past results to "auto-complete" what a user
 * is actively inputting into the system
 */
interface AutoCompleteView<Type> {

    /**
     * Gets an [Observable], which contains a stream of text changes
     *
     * @param field An [AutoCompleteField] to get the stream for
     * @return An [Observable], which will emit a new [CharSequence] whenever the user changes the
     * text for a given field
     */
    fun getTextChangeStream(field: AutoCompleteField) : Observable<CharSequence>

    /**
     * Indicates that we should display a list of auto-completion results
     *
     * @param field the [AutoCompleteField] to displays these results for
     * @param results a [List] of [AutoCompleteResult] of [Type]
     */
    @UiThread
    fun displayAutoCompleteResults(field: AutoCompleteField, results: MutableList<AutoCompleteResult<Type>>)

    val hideAutoCompleteVisibilityClick: Observable<AutoCompleteUpdateEvent<Type>>
    val unHideAutoCompleteVisibilityClick: Observable<AutoCompleteUpdateEvent<Type>>

    fun fillValueField(autoCompleteResult: AutoCompleteResult<Type>)
    fun sendAutoCompleteHideEvent(autoCompleteResult: AutoCompleteResult<Type>)
    fun removeValueFromAutoComplete(position: Int)
    fun sendAutoCompleteUnHideEvent(position: Int)
    fun displayAutoCompleteError()
}