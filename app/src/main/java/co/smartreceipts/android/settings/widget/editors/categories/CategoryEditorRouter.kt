package co.smartreceipts.android.settings.widget.editors.categories

import android.support.annotation.UiThread


/**
 * Defines a contract for routes from when the [CategoryEditorView] is active
 */
interface CategoryEditorRouter {

    /**
     * Indicates that we should dismiss the editor
     */
    @UiThread
    fun dismissEditor()
}