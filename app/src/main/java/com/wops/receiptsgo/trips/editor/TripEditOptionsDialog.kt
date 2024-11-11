package com.wops.receiptsgo.trips.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.wops.receiptsgo.databinding.DialogTripEditBinding
import com.wops.receiptsgo.widget.dialog.BaseBottomSheetDialog

enum class TripEditOption {EDIT, DELETE}

class TripEditOptionsDialog : BaseBottomSheetDialog() {

    companion object {
        const val TAG = "TripEditOptionsDialog"

        const val REQUEST_KEY = "tripEditOptionRequest"
        const val RESULT_KEY = "tripEditOptionResult"

        private const val TRIP_NAME_KEY = "tripName"

        @JvmStatic
        fun newInstance(tripName: String): TripEditOptionsDialog {
            val dialog = TripEditOptionsDialog()
            dialog.arguments = bundleOf(Pair(TRIP_NAME_KEY, tripName))

            return dialog
        }
    }

    private var _binding: DialogTripEditBinding? = null
    private val binding get() = _binding!!

    private val tripName: String
        get() = requireArguments().getString(TRIP_NAME_KEY)!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogTripEditBinding.inflate(inflater, container, false)

        binding.tripName.text = tripName

        binding.tripEdit.setOnClickListener{setResult(TripEditOption.EDIT)}
        binding.tripDelete.setOnClickListener{setResult(TripEditOption.DELETE)}

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setResult(option: TripEditOption) {
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(Pair(RESULT_KEY, option.name)))

        dismiss()
    }
}