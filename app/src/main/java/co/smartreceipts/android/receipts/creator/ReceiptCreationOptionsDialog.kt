package co.smartreceipts.android.receipts.creator

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.smartreceipts.android.databinding.DialogReceiptCreationBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

enum class ReceiptCreationOption { CAMERA, GALLERY, PDF, TEXT }

class ReceiptCreationOptionsDialog : BottomSheetDialogFragment() {

    private var _binding: DialogReceiptCreationBinding? = null
    private val binding get() = _binding!!


    companion object {
        const val TAG = "ReceiptCreationOptionsDialog"

        const val REQUEST_KEY = "receiptCreationOptionRequest"
        const val RESULT_KEY = "receiptCreationOptionResult"

        @JvmStatic
        fun newInstance(): ReceiptCreationOptionsDialog = ReceiptCreationOptionsDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogReceiptCreationBinding.inflate(inflater, container, false)

        binding.newImageCamera.setOnClickListener { setResult(ReceiptCreationOption.CAMERA) }
        binding.newImportImage.setOnClickListener { setResult(ReceiptCreationOption.GALLERY) }
        binding.newImportPdf.setOnClickListener { setResult(ReceiptCreationOption.PDF) }
        binding.newText.setOnClickListener { setResult(ReceiptCreationOption.TEXT) }

        return binding.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val behavior = dialog.behavior
        behavior.isFitToContents = true
        behavior.skipCollapsed = true // allow ho skip collapsed state when the dialog is dismissing by swipe

        // hack to set skip collapsed state when the dialog is opening
        dialog.setOnShowListener { d ->
            (d as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return dialog
    }

    private fun setResult(option: ReceiptCreationOption) {
        val bundle = Bundle()
        bundle.putString(RESULT_KEY, option.name)

        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundle)

        dismiss()
    }
}