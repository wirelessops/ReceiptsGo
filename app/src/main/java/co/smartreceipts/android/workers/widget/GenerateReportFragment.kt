package co.smartreceipts.android.workers.widget

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.databinding.GenerateReportLayoutBinding
import co.smartreceipts.android.fragments.FabClickListener
import co.smartreceipts.android.fragments.ReportInfoFragment
import co.smartreceipts.android.fragments.WBFragment
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.workers.EmailAssistant.EmailOptions
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import wb.android.flex.Flex
import java.util.*
import javax.inject.Inject

class GenerateReportFragment : GenerateReportView, WBFragment(), FabClickListener {

    @Inject
    lateinit var presenter: GenerateReportPresenter

    @Inject
    lateinit var flex: Flex

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>


    private lateinit var pdfFullCheckbox: CheckBox
    private lateinit var pdfImagesCheckbox: CheckBox
    private lateinit var csvCheckbox: CheckBox
    private lateinit var zipCheckbox: CheckBox
    private lateinit var zipWithMetadataCheckbox: CheckBox

    private var _binding: GenerateReportLayoutBinding? = null
    private val binding get() = _binding!!

    private val fabClicks = PublishSubject.create<Unit>()

    override val generateReportClicks: Observable<EnumSet<EmailOptions>>
        get() = fabClicks.map {
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

        binding.generateReportTooltip.setOnClickListener {
            analytics.record(Events.Informational.ConfigureReport)
            navigationHandler.navigateToSettingsScrollToReportSection()
        }


        val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            val drawableEnd: Drawable? = buttonView.compoundDrawablesRelative[2]
            if (drawableEnd != null) {
                drawableEnd.mutate()
                val drawableColor = ContextCompat.getColor(
                    requireContext(),
                    if (isChecked) R.color.smart_receipts_colorPrimary else R.color.navigation_inactive
                )
                DrawableCompat.setTint(drawableEnd, drawableColor)
            }


            val bgColor =
                ContextCompat.getColor(requireContext(), if (isChecked) R.color.receipt_image_tint else R.color.bottom_navigation_color)
            val parent: ViewParent = buttonView.parent
            if (parent is FrameLayout) {
                parent.setBackgroundColor(bgColor)
            }
        }

        pdfFullCheckbox.setOnCheckedChangeListener(onCheckedChangeListener)
        pdfImagesCheckbox.setOnCheckedChangeListener(onCheckedChangeListener)
        csvCheckbox.setOnCheckedChangeListener(onCheckedChangeListener)
        zipCheckbox.setOnCheckedChangeListener(onCheckedChangeListener)
        zipWithMetadataCheckbox.setOnCheckedChangeListener(onCheckedChangeListener)

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
                binding.progress.visibility = View.GONE

                try {
                    startActivityForResult(
                        Intent.createChooser(result.intent, requireContext().getString(R.string.send_email)),
                        SHARE_REPORT_REQUEST_CODE
                    )
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
                binding.progress.visibility = View.GONE
                handleGenerationError(result.errorType)
            }

            EmailResult.InProgress -> {
                binding.progress.visibility = View.VISIBLE
            }
        }
    }

    override fun onFabClick() {
        fabClicks.onNext(Unit)
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

            GenerationErrors.ERROR_PDF_GENERATION -> Toast.makeText(context, R.string.report_pdf_generation_error, Toast.LENGTH_SHORT)
                .show()

            GenerationErrors.ERROR_MEMORY -> Toast.makeText(context, R.string.report_error_memory, Toast.LENGTH_LONG).show()

            GenerationErrors.ERROR_UNDETERMINED -> Toast.makeText(context, R.string.report_error_undetermined, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SHARE_REPORT_REQUEST_CODE -> presenter.showInterstitialAd()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): GenerateReportFragment {
            return GenerateReportFragment()
        }

        private const val SHARE_REPORT_REQUEST_CODE = 486
    }
}