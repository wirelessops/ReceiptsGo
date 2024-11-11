package com.wops.receiptsgo.settings.widget.editors.categories

import androidx.annotation.UiThread
import com.wops.receiptsgo.model.Category
import io.reactivex.Observable

/**
 * Defines a contract for our [Category] editor, which we use to create new [Category] instances or
 * save existing ones
 */
interface CategoryEditorView {

    /**
     * Informs our view to display a particular [Category] optional
     *
     * @param category the [Category] or null if we're creating a new one
     */
    @UiThread
    fun displayCategory(category: Category?)

    /**
     * @return the [Category.name] [String]
     */
    fun getName() : String

    /**
     * @return the [Category.code] [String]
     */
    fun getCode() : String

    /**
     * @return an [Observable], which will emit an [Any] whenever the user clicks the save button
     */
    fun getSaveClickStream() : Observable<Any>

    /**
     * @return an [Observable], which will emit an [Any] whenever the user clicks the cancel button
     */
    fun getCancelClickStream() : Observable<Any>
}