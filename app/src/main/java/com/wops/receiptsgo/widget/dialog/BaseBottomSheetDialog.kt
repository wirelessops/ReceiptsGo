package com.wops.receiptsgo.widget.dialog

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val behavior = dialog.behavior
        behavior.isFitToContents = true
        behavior.skipCollapsed = true // allows ho skip collapsed state when the dialog is dismissing by swipe

        // hack to skip collapsed state when the dialog is opening
        dialog.setOnShowListener { d ->
            (d as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return dialog
    }
}