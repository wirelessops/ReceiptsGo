package com.wops.receiptsgo.workers.widget

import android.app.Activity
import android.app.AlertDialog
import android.app.AlertDialog.*
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.snackbar.Snackbar
import com.wops.analytics.Analytics
import com.wops.analytics.events.Events
import com.wops.analytics.log.Logger
import com.wops.receiptsgo.R
import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.receiptsgo.databinding.GenerateReportLayoutBinding
import com.wops.receiptsgo.fragments.FabClickListener
import com.wops.receiptsgo.fragments.ReportInfoFragment
import com.wops.receiptsgo.fragments.WBFragment
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.workers.EmailAssistant.EmailOptions
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
    lateinit var navigationHandler: NavigationHandler<ReceiptsGoActivity>

    private var pdfFullCheckbox: CheckBox? = null
    private var pdfImagesCheckbox: CheckBox? = null
    private var csvCheckbox: CheckBox? = null
    private var zipCheckbox: CheckBox? = null
    private var zipWithMetadataCheckbox: CheckBox? = null
    private var saveToDeviceSwitch: SwitchCompat? = null

    private var _binding: GenerateReportLayoutBinding? = null
    private val binding get() = _binding!!

    private val fabClicks = PublishSubject.create<Unit>()
    private val reportShares = PublishSubject.create<Unit>()

    override val generateReportClicks: Observable<EnumSet<EmailOptions>>
        get() = fabClicks.map {
            val options = EnumSet.noneOf(EmailOptions::class.java)

            if (pdfFullCheckbox!!.isChecked) options.add(EmailOptions.PDF_FULL)
            if (pdfImagesCheckbox!!.isChecked) options.add(EmailOptions.PDF_IMAGES_ONLY)
            if (csvCheckbox!!.isChecked) options.add(EmailOptions.CSV)
            if (zipWithMetadataCheckbox!!.isChecked) options.add(EmailOptions.ZIP_WITH_METADATA)
            if (zipCheckbox!!.isChecked) options.add(EmailOptions.ZIP)

            if (saveToDeviceSwitch!!.isChecked) options.add(EmailOptions.SAVE_TO_DEVICE)


            return@map options
        }

    override val reportSharedEvents: Observable<Unit>
        get() = reportShares

    override val getActivity: Activity
        get() = requireActivity()

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

        saveToDeviceSwitch = binding.generateReportSaveSwitch

        binding.generateReportTooltip.setOnClickListener {
            analytics.record(Events.Informational.ConfigureReport)
            navigationHandler.navigateToSettingsScrollToReportSection()
        }


           // saveToDeviceSwitch!!.setOnCheckedChangeListener { buttonView, isChecked ->
        val fabButton: FloatingActionButton? =
            parentFragment?.view?.findViewById(R.id.fab)

        val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when (buttonView) {
                saveToDeviceSwitch -> {
                    try {
                        val actualUriPermissions = context?.contentResolver?.persistedUriPermissions
                        val mostRecentUriPermission =
                            actualUriPermissions?.maxBy { it.persistedTime }
                                .takeIf { it?.isWritePermission == true }
                    }
                    catch (e: NoSuchElementException) { // Thrown by maxBy
                        buttonView.isChecked = false
                        Toast.makeText(
                            context,
                            getString(R.string.toast_save_to_device_permission_rationale),
                            Toast.LENGTH_LONG
                        ).show();
                        
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

                        val initalUri = context?.getFilePickerInitialUri(Environment.DIRECTORY_DOCUMENTS + "/")
                        initalUri?.let {
                            intent.putExtra("android.provider.extra.INITIAL_URI", initalUri)
                        }
                        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE)
                    }

                    if (buttonView.isChecked) {
                        fabButton?.setImageResource(R.drawable.download_24px)
                    } else {
                        fabButton?.setImageResource(R.drawable.ic_share)
                    }
                }

                else -> {
                    val drawableEnd: Drawable? = buttonView.compoundDrawablesRelative[2]
                    if (drawableEnd != null) {
                        drawableEnd.mutate()
                        val drawableColor = ContextCompat.getColor(
                            requireContext(),
                            if (isChecked) R.color.navigation_active else R.color.navigation_inactive
                        )
                        DrawableCompat.setTint(drawableEnd, drawableColor)
                    }

                    val bgColor =
                        ContextCompat.getColor(
                            requireContext(),
                            if (isChecked) R.color.selected_card_background else R.color.card_background
                        )
                    val parent: ViewParent = buttonView.parent
                    if (parent is FrameLayout) {
                        parent.setBackgroundColor(bgColor)
                    }
                }
            }
        }

        pdfFullCheckbox!!.setOnCheckedChangeListener(onCheckedChangeListener)
        pdfImagesCheckbox!!.setOnCheckedChangeListener(onCheckedChangeListener)
        csvCheckbox!!.setOnCheckedChangeListener(onCheckedChangeListener)
        zipCheckbox!!.setOnCheckedChangeListener(onCheckedChangeListener)
        zipWithMetadataCheckbox!!.setOnCheckedChangeListener(onCheckedChangeListener)

        saveToDeviceSwitch!!.setOnCheckedChangeListener(onCheckedChangeListener)

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
                if (result.uris != null) {
                    // Files were save to device
                    Snackbar.make(
                        binding.root,
                        "Files saved",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("View", View.OnClickListener {
                            val webIntent: Intent = result.uris.first().let { uri ->
                                Intent(Intent.ACTION_VIEW, uri)
                            }.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)


                            if (result.uris.size > 1) {
                                var directoryUri = DocumentsContract.buildDocumentUriUsingTree(result.uris.first(),
                                    DocumentsContract.getTreeDocumentId(result.uris.first()))
                                webIntent.setDataAndType(directoryUri, "vnd.android.document/directory")
                            }
                            startActivity(webIntent)
                        }

                        ).setActionTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary_color))
                        .show()
                } else {
                    try {

                        startActivityForResult(
                            Intent.createChooser(
                                result.intent,
                                requireContext().getString(R.string.send_email)
                            ),
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
                Builder(context).setTitle(R.string.report_pdf_error_too_many_columns_title)
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
            GenerationErrors.ERROR_FILE_COPY -> TODO()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        pdfFullCheckbox = null
        pdfImagesCheckbox = null
        csvCheckbox = null
        zipCheckbox = null
        zipWithMetadataCheckbox = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        when (requestCode) {
//            SHARE_REPORT_REQUEST_CODE -> reportShares.onNext(Unit)
//        }

        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val directoryUri = data?.data ?: return

            val contentResolver = context?.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            contentResolver?.takePersistableUriPermission(
                directoryUri,
                (takeFlags)
            )
            saveToDeviceSwitch?.isChecked = true
            //println(directoryUri)

            //val documentsTree = DocumentFile.fromTreeUri(requireContext(), directoryUri)
            //println(documentsTree.toString())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): GenerateReportFragment {
            return GenerateReportFragment()
        }

        private const val SHARE_REPORT_REQUEST_CODE = 486
        private const val OPEN_DIRECTORY_REQUEST_CODE = 1233


        // From https://stackoverflow.com/a/77087157
        private fun Context.getFilePickerInitialUri(directoryPath: String): Uri? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                // you can't set directory programmatically on Android 7.1 or lower, so just return null
                return null
            }

            val rootPath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // you can't get the root path from the system on old Android, use a hardcoded string
                "content://com.android.externalstorage.documents/document/primary%3A"
            } else {
                // you can get the root path from the system on Android 10+, get it with intent
                val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val testIntent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()

                val systemDefaultPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    testIntent.getParcelableExtra("android.provider.extra.INITIAL_URI", Uri::class.java)
                } else {
                    @Suppress("Deprecation")
                    testIntent.getParcelableExtra("android.provider.extra.INITIAL_URI")
                }

                systemDefaultPath.toString().replace("/root/", "/document/") + "%3A"
            }

            return Uri.parse(rootPath + directoryPath)
        }
    }
}