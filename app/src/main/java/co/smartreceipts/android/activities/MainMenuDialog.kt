package co.smartreceipts.android.activities

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import co.smartreceipts.android.databinding.DialogMainMenuBinding
import co.smartreceipts.android.widget.dialog.BaseBottomSheetDialog
import kotlinx.parcelize.Parcelize

enum class MainMenuOption { SUBSCRIPTIONS, SETTINGS, OCR_CONFIGURATION, BACKUP, PRO_SUBSCRIPTION, USAGE_GUIDE, MY_ACCOUNT }

@Parcelize
class MainMenuDialogConfig(
    val hideProSubscription: Boolean = false,
    val hideSettings: Boolean = false,
    val hideOcr: Boolean = false,
    val hideMyAccount: Boolean = false,
    val hideSubscriptions: Boolean = false,
) : Parcelable

class MainMenuDialog : BaseBottomSheetDialog() {

    companion object {
        const val TAG = "MainMenuDialog"

        const val REQUEST_KEY = "MainMenuOptionRequest"
        const val RESULT_KEY = "MainMenuOptionResult"

        private const val CONFIG_KEY = "CONFIG_KEY"

        @JvmStatic
        fun newInstance(config: MainMenuDialogConfig): MainMenuDialog = MainMenuDialog().apply {
            arguments = bundleOf(CONFIG_KEY to config)
        }
    }

    private var _binding: DialogMainMenuBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogMainMenuBinding.inflate(inflater, container, false)

        val config = BundleCompat.getParcelable(requireArguments(), CONFIG_KEY, MainMenuDialogConfig::class.java)

        binding.menuMainDialogSubscriptions.isGone = config?.hideSubscriptions == true
        binding.menuMainDialogSettings.isGone = config?.hideSettings == true
        binding.menuMainDialogOcrConfiguration.isGone = config?.hideOcr == true
        binding.menuMainDialogMyAccount.isGone = config?.hideMyAccount == true
        binding.menuMainDialogProSubscription.isGone = config?.hideProSubscription == true

        binding.menuMainDialogSubscriptions.setOnClickListener { setResult(MainMenuOption.SUBSCRIPTIONS) }
        binding.menuMainDialogSettings.setOnClickListener { setResult(MainMenuOption.SETTINGS) }
        binding.menuMainDialogOcrConfiguration.setOnClickListener { setResult(MainMenuOption.OCR_CONFIGURATION) }
        binding.menuMainDialogBackup.setOnClickListener { setResult(MainMenuOption.BACKUP) }
        binding.menuMainDialogProSubscription.setOnClickListener { setResult(MainMenuOption.PRO_SUBSCRIPTION) }
        binding.menuMainDialogUsageGuide.setOnClickListener { setResult(MainMenuOption.USAGE_GUIDE) }
        binding.menuMainDialogMyAccount.setOnClickListener { setResult(MainMenuOption.MY_ACCOUNT) }
        binding.menuMainDialogCancel.setOnClickListener { dismiss() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setResult(option: MainMenuOption) {
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(Pair(RESULT_KEY, option.name)))

        dismiss()
    }
}