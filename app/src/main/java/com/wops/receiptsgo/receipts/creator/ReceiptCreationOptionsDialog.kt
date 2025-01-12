package com.wops.receiptsgo.receipts.creator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.wops.receiptsgo.databinding.DialogReceiptCreationBinding
import com.wops.receiptsgo.widget.dialog.BaseBottomSheetDialog

enum class ReceiptCreationOption { CAMERA, GALLERY, PDF, TEXT }

class ReceiptCreationOptionsDialog : BaseBottomSheetDialog() {

    companion object {
        const val TAG = "ReceiptCreationOptionsDialog"

        const val REQUEST_KEY = "receiptCreationOptionRequest"
        const val RESULT_KEY = "receiptCreationOptionResult"

        @JvmStatic
        fun newInstance(): ReceiptCreationOptionsDialog = ReceiptCreationOptionsDialog()
    }

    private var _binding: DialogReceiptCreationBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogReceiptCreationBinding.inflate(inflater, container, false)

        binding.newImageCamera.setOnClickListener { setResult(ReceiptCreationOption.CAMERA) }
        binding.newImportImage.setOnClickListener { setResult(ReceiptCreationOption.GALLERY) }
        binding.newImportPdf.setOnClickListener { setResult(ReceiptCreationOption.PDF) }
        binding.newText.setOnClickListener { setResult(ReceiptCreationOption.TEXT) }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setResult(option: ReceiptCreationOption) {
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(Pair(RESULT_KEY, option.name)))

        dismiss()
    }
}