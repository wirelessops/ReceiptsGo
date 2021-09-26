package co.smartreceipts.android.receipts.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import co.smartreceipts.android.databinding.DialogReceiptEditBinding
import co.smartreceipts.android.widget.dialog.BaseBottomSheetDialog

enum class ReceiptEditOption { EDIT, COPY_MOVE, SWAP_UP, SWAP_DOWN, DELETE_ATTACHMENT, DELETE }

class ReceiptEditOptionsDialog : BaseBottomSheetDialog() {

    companion object {
        const val TAG = "ReceiptEditOptionsDialog"

        const val REQUEST_KEY = "receiptEditOptionRequest"
        const val RESULT_KEY = "receiptEditOptionResult"

        private const val RECEIPT_NAME_KEY = "receiptName"
        private const val ATTACHMENT_KEY = "hasAttachment"

        @JvmStatic
        fun newInstance(receiptName: String, hasAttachment: Boolean): ReceiptEditOptionsDialog {
            val dialog = ReceiptEditOptionsDialog()
            dialog.arguments = bundleOf(Pair(RECEIPT_NAME_KEY, receiptName), Pair(ATTACHMENT_KEY, hasAttachment))

            return dialog
        }
    }

    private var _binding: DialogReceiptEditBinding? = null
    private val binding get() = _binding!!

    private val receiptName: String
        get() = requireArguments().getString(RECEIPT_NAME_KEY)!!

    private val hasAttachment: Boolean
        get() = requireArguments().getBoolean(ATTACHMENT_KEY)



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogReceiptEditBinding.inflate(inflater, container, false)

        binding.receiptName.text = receiptName

        binding.receiptEdit.setOnClickListener { setResult(ReceiptEditOption.EDIT) }
        binding.receiptMoveCopy.setOnClickListener { setResult(ReceiptEditOption.COPY_MOVE) }
        binding.receiptSwapUp.setOnClickListener { setResult(ReceiptEditOption.SWAP_UP) }
        binding.receiptSwapDown.setOnClickListener { setResult(ReceiptEditOption.SWAP_DOWN) }
        binding.receiptDelete.setOnClickListener { setResult(ReceiptEditOption.DELETE) }

        if (!hasAttachment) {
            binding.receiptDeleteAttachment.visibility = View.GONE
        } else {
            binding.receiptDeleteAttachment.setOnClickListener { setResult(ReceiptEditOption.DELETE_ATTACHMENT) }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setResult(option: ReceiptEditOption) {
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(Pair(RESULT_KEY, option.name)))

        dismiss()
    }
}