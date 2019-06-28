package co.smartreceipts.android.receipts.editor.toolbar

import androidx.annotation.UiThread


/**
 * A simple view contract that allows us to easily display our title or subtitle
 */
interface ReceiptsEditorToolbarView {

    /**
     * Informs our receipts editor to display a particular title
     *
     * @param title the title [String] to display
     */
    @UiThread
    fun displayTitle(title: String)

}