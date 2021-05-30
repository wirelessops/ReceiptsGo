package co.smartreceipts.android.settings.widget.editors.payment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.DialogPaymentMethodEditorBinding
import co.smartreceipts.android.model.PaymentMethod


/**
 * A [DialogFragment], which allows the user to create a new [PaymentMethod] or edit an existing one
 */
class PaymentMethodEditorDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    private val paymentMethod: PaymentMethod?
        get() = arguments?.getParcelable(PaymentMethod.PARCEL_KEY)

    private var container: ViewGroup? = null

    private var _binding: DialogPaymentMethodEditorBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.container = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPaymentMethodEditorBinding.inflate(LayoutInflater.from(activity), container, false)

        binding.paymentMethodName.setText(paymentMethod?.method ?: "")
        binding.isReimbursable.isChecked = paymentMethod?.isReimbursable ?: false

        @StringRes val title = if (paymentMethod == null) R.string.payment_method_add else R.string.payment_method_edit
        @StringRes val positiveButton = if (paymentMethod == null) R.string.add else R.string.update

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(title)
        builder.setView(binding.root)
        builder.setPositiveButton(positiveButton, this)
        builder.setNegativeButton(android.R.string.cancel, this)
        return builder.create()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> save()
            DialogInterface.BUTTON_NEGATIVE -> dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun save() {
        val name = binding.paymentMethodName.text?.toString() ?: ""

        if (name.isEmpty()) return

        val bundle = bundleOf(
            Pair(PaymentMethod.PARCEL_KEY, paymentMethod),
            Pair(RESULT_NAME_KEY, name),
            Pair(RESULT_IS_REIMBURSABLE_KEY, binding.isReimbursable.isChecked)
        )

        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundle)

        dismiss()
    }

    companion object {

        const val TAG = "PaymentMethodEditorDialogFragment"

        const val REQUEST_KEY = "paymentMethodEditorRequest"
        const val RESULT_NAME_KEY = "paymentMethodNameResult"
        const val RESULT_IS_REIMBURSABLE_KEY = "paymentMethodIsReimbursableResult"


        @JvmStatic
        fun newInstance(paymentMethod: PaymentMethod?): PaymentMethodEditorDialogFragment {
            val fragment = PaymentMethodEditorDialogFragment()
            val args = Bundle()
            args.putParcelable(PaymentMethod.PARCEL_KEY, paymentMethod)
            fragment.arguments = args
            return fragment
        }
    }
}