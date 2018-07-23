package co.smartreceipts.android.autocomplete

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter


/**
 * Modifies the core [ArrayAdapter] contract to address a bug that is specific to auto-completion
 */
class AutoCompleteArrayAdapter<Type>(context: Context,
                                     autoCompleteResults: List<AutoCompleteResult<Type>>)
    : ArrayAdapter<AutoCompleteResult<Type>>(context, android.R.layout.simple_dropdown_item_1line, autoCompleteResults) {

    /**
     * Note: We override the default ArrayAdapter$ArrayFilter logic here, since this filter object's
     * [Filter.publishResults] method call will invalidate this adapter if our count is ever 0. This
     * introduces a issue if the user types all the way to the end of the results and then deletes a
     * character or two, since we'll now be using an invalidated set of results. As a result, we have
     * overridden this method to instead call [notifyDataSetChanged] if the [getCount] result is 0
     * when this method is called
     */
    override fun notifyDataSetInvalidated() {
        if (count != 0) {
            super.notifyDataSetInvalidated()
        } else {
            super.notifyDataSetChanged()
        }
    }
}