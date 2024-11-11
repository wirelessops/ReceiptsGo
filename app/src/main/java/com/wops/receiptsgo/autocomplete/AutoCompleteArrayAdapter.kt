package com.wops.receiptsgo.autocomplete

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.wops.receiptsgo.R
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

/**
 * Modifies the core [ArrayAdapter] contract to address a bug that is specific to auto-completion
 */
class AutoCompleteArrayAdapter<Type>(context: Context,
                                     autoCompleteResults: MutableList<AutoCompleteResult<Type>>,
                                     private val view: AutoCompleteView<Type>)
    : ArrayAdapter<AutoCompleteResult<Type>>(context, R.layout.auto_complete_view, autoCompleteResults) {

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItem = convertView ?: LayoutInflater.from(context).inflate(R.layout.auto_complete_view, parent, false)
        val result = getItem(position)

        val name = listItem.findViewById(R.id.auto_complete_name) as TextView
        name.text = result!!.displayName
        name.setOnClickListener {
            view.fillValueField(result)
        }

        val image = listItem.findViewById(R.id.imgAutoCompleteDelete) as ImageView
        image.setOnClickListener {
            view.sendAutoCompleteHideEvent(result)
        }
        return listItem
    }
}