package co.smartreceipts.android.workers.widget

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.databinding.GenerateReportLayoutBinding
import co.smartreceipts.android.fragments.ReportInfoFragment
import co.smartreceipts.android.fragments.WBFragment
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.workers.EmailAssistant.EmailOptions
import com.google.common.base.Preconditions
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import wb.android.flex.Flex
import java.util.*
import javax.inject.Inject

class GenerateReportFragment : GenerateReportView, WBFragment() {

    @Inject
    lateinit var presenter: GenerateReportPresenter


    @Inject
    lateinit var flex: Flex

    @Inject
    lateinit var analytics: Analytics // to presenter

    @Inject
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>


    private lateinit var pdfFullCheckbox: CheckBox
    private lateinit var pdfImagesCheckbox: CheckBox
    private lateinit var csvCheckbox: CheckBox
    private lateinit var zipCheckbox: CheckBox
    private lateinit var zipWithMetadataCheckbox: CheckBox
    private lateinit var progress: ProgressBar

    private var _binding: GenerateReportLayoutBinding? = null
    private val binding get() = _binding!!


    override val generateReportClicks: Observable<EnumSet<EmailOptions>>
        get() = binding.receiptActionSend.clicks()
            .map {
                val options = EnumSet.noneOf(EmailOptions::class.java)

                if (pdfFullCheckbox.isChecked) options.add(EmailOptions.PDF_FULL)
                if (pdfImagesCheckbox.isChecked) options.add(EmailOptions.PDF_IMAGES_ONLY)
                if (csvCheckbox.isChecked) options.add(EmailOptions.CSV)
                if (zipWithMetadataCheckbox.isChecked) options.add(EmailOptions.ZIP_WITH_METADATA)
                if (zipCheckbox.isChecked) options.add(EmailOptions.ZIP)

                return@map options
            }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = GenerateReportLayoutBinding.inflate(inflater, container, false)

        val root: View = binding.root

        pdfFullCheckbox = flex.getSubView(activity, root, R.id.dialog_email_checkbox_pdf_full) as CheckBox
        pdfImagesCheckbox = flex.getSubView(activity, root, R.id.dialog_email_checkbox_pdf_images) as CheckBox
        csvCheckbox = flex.getSubView(activity, root, R.id.dialog_email_checkbox_csv) as CheckBox
        zipWithMetadataCheckbox = flex.getSubView(activity, root, R.id.dialog_email_checkbox_zip_with_metadata) as CheckBox
        zipCheckbox = binding.dialogEmailCheckboxZip
        progress = binding.progress

        binding.generateReportTooltip.setOnClickListener {
            analytics.record(Events.Informational.ConfigureReport)
            navigationHandler.navigateToSettingsScrollToReportSection()
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        presenter.subscribe(getParentTrip())
    }

    override fun onStop() {
        presenter.unsubscribe()
        super.onStop()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            val actionBar = supportActionBar
            if (actionBar != null) {
                actionBar.subtitle = null
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Logger.debug(this, "pre-onSaveInstanceState")
        super.onSaveInstanceState(outState)
        Logger.debug(this, "onSaveInstanceState")
    }

    override fun present(result: EmailResult) {

        when (result) {
            is EmailResult.Success -> {
                progress.visibility = View.GONE

                try {
                    startActivity(Intent.createChooser(result.intent, requireContext().getString(R.string.send_email)))
                } catch (e: ActivityNotFoundException) {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle(R.string.error_no_send_intent_dialog_title)
                        .setMessage(
                            requireContext().getString(
                                R.string.error_no_send_intent_dialog_message,
                                getParentTrip().directory.absolutePath
                            )
                        )
                        .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                        .show()
                }
            }

            is EmailResult.Error -> {
                progress.visibility = View.GONE
                handleGenerationError(result.errorType)
            }

            EmailResult.InProgress -> {
                progress.visibility = View.VISIBLE
            }
        }
    }

    private fun getParentTrip(): Trip {
        return (parentFragment as ReportInfoFragment?)!!.trip
    }

    private fun handleGenerationError(error: GenerationErrors) {
        when (error) {
            GenerationErrors.ERROR_NO_SELECTION -> {
                Toast.makeText(context, flex.getString(context, R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show()
            }

            GenerationErrors.ERROR_NO_RECEIPTS -> {
                Toast.makeText(context, flex.getString(context, R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show()
            }

            GenerationErrors.ERROR_DISABLED_DISTANCES -> {
                Toast.makeText(
                    context, requireContext().getString(
                        R.string.toast_csv_report_distances, requireContext().getString(R.string.pref_distance_print_table_title)
                    ), Toast.LENGTH_SHORT
                ).show()

                navigationHandler.navigateToSettingsScrollToDistanceSection()
            }

            GenerationErrors.ERROR_TOO_MANY_COLUMNS -> {
                val messageId = when {
                    presenter.isLandscapeReportEnabled() -> R.string.report_pdf_error_too_many_columns_message
                    else -> R.string.report_pdf_error_too_many_columns_message_landscape
                }
                AlertDialog.Builder(context).setTitle(R.string.report_pdf_error_too_many_columns_title)
                    .setMessage(messageId)
                    .setPositiveButton(R.string.report_pdf_error_go_to_settings) { dialog, _ ->
                        dialog.cancel()
                        navigationHandler.navigateToSettingsScrollToReportSection()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }

            GenerationErrors.ERROR_PDF_GENERATION -> Toast.makeText(context, R.string.report_pdf_generation_error, Toast.LENGTH_SHORT).show()

            GenerationErrors.ERROR_MEMORY -> Toast.makeText(context, R.string.report_error_memory, Toast.LENGTH_LONG).show()

            GenerationErrors.ERROR_UNDETERMINED -> Toast.makeText(context, R.string.report_error_undetermined, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): GenerateReportFragment {
            return GenerateReportFragment()
        }
    }
}