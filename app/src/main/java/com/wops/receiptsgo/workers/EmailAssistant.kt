package com.wops.receiptsgo.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.service.chooser.ChooserAction
import android.util.Log
import com.wops.analytics.log.Logger
import com.wops.receiptsgo.R
import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.persistence.DatabaseHelper
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.utils.IntentUtils
import com.wops.receiptsgo.workers.reports.formatting.SmartReceiptsFormattableString
import com.wops.receiptsgo.workers.widget.EmailResult
import com.wops.receiptsgo.workers.widget.GenerationErrors
import com.wops.core.di.scopes.ApplicationScope
import com.hadisatrio.optional.Optional
import com.wops.core.utils.UriUtils
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import javax.inject.Inject

@ApplicationScope
class EmailAssistant @Inject constructor(
    private val context: Context,
    private val databaseHelper: DatabaseHelper,
    private val preferenceManager: UserPreferenceManager,
    private val attachmentFilesWriter: AttachmentFilesWriter,
    private val dateFormatter: DateFormatter,
    private val intentUtils: IntentUtils
) {

    enum class EmailOptions(val index: Int) {
        PDF_FULL(0), PDF_IMAGES_ONLY(1), CSV(2), ZIP(3), ZIP_WITH_METADATA(4), SAVE_TO_DEVICE(5);
    }

    companion object {
        private const val DEVELOPER_EMAIL = "de" + "v@" + "wire" + "lessops" + "." + "com"

        private fun getEmailDeveloperIntent(): Intent {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$DEVELOPER_EMAIL")
            return intent
        }

        @JvmStatic
        fun getEmailDeveloperIntent(subject: String): Intent {
            val intent = getEmailDeveloperIntent()
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            return intent
        }

        @JvmStatic
        fun getEmailDeveloperIntent(subject: String, body: String): Intent {
            val intent = getEmailDeveloperIntent(subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            return intent
        }

        @JvmStatic
        fun getEmailDeveloperIntent(context: Context, subject: String, body: String, files: List<File>): Intent {
            val intent = IntentUtils().getSendIntent(context, files)
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            return intent
        }
    }

    fun emailTrip(trip: Trip, options: EnumSet<EmailOptions>): Single<EmailResult> {
        if (options.singleOrNull() == EmailOptions.SAVE_TO_DEVICE ) {
            return Single.just(EmailResult.Error(GenerationErrors.ERROR_NO_SELECTION))
        }

        return Single.zip(
            databaseHelper.receiptsTable.get(trip, true),
            databaseHelper.distanceTable.get(trip, true),
            BiFunction<List<Receipt>, List<Distance>, EmailResult> { receipts, distances ->
                val preGenerationIssues = checkPreGenerationIssues(receipts, distances, options)

                when {
                    preGenerationIssues.isPresent -> preGenerationIssues.get()
                    else -> writeReport(trip, receipts, distances, options)
                }
            })
    }


    private fun checkPreGenerationIssues(
        receipts: List<Receipt>,
        distances: List<Distance>,
        options: EnumSet<EmailOptions>
    ): Optional<EmailResult.Error> {
        if (receipts.isEmpty()) {
            if (distances.isEmpty() || !(options.contains(EmailOptions.CSV) || options.contains(EmailOptions.PDF_FULL))) {
                // Only allow report processing to continue with no receipts if we're doing a full pdf or CSV report with distances
                return Optional.of(EmailResult.Error(GenerationErrors.ERROR_NO_RECEIPTS))
            } else {
                if ((options.contains(EmailOptions.CSV) || options.contains(EmailOptions.PDF_FULL))
                    && !preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)) {
                    // user wants to create CSV or PDF report with just distances but this option is disabled
                    return Optional.of(EmailResult.Error(GenerationErrors.ERROR_DISABLED_DISTANCES))
                }
            }
        }

        return Optional.absent()
    }

    private fun writeReport(trip: Trip, receipts: List<Receipt>, distances: List<Distance>, options: EnumSet<EmailOptions>): EmailResult {
        val writerResults = attachmentFilesWriter.write(trip, receipts, distances, options)

        return when {
            writerResults.didMemoryErrorOccure -> EmailResult.Error(GenerationErrors.ERROR_MEMORY)
            writerResults.didPDFFailCompletely -> {
                if (writerResults.didPDFFailTooManyColumns) {
                    EmailResult.Error(GenerationErrors.ERROR_TOO_MANY_COLUMNS)
                } else {
                    EmailResult.Error(GenerationErrors.ERROR_PDF_GENERATION)
                }
            }
            else -> {
                if (options.contains(EmailOptions.SAVE_TO_DEVICE)) {
                    try {
                        val uriList = copyFilesToDevice(writerResults.files.filterNotNull(), trip, receipts, distances)
                        Log.d("SaveToDevice", "URL List: $uriList")
                        EmailResult.Success(uris = uriList)
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                        EmailResult.Error(GenerationErrors.ERROR_FILE_COPY)

                    }
                }
                else {
                    val sendIntent = prepareSendAttachmentsIntent(
                        writerResults.files.filterNotNull(),
                        trip,
                        receipts,
                        distances
                    )
                    //val sendIntent = prepareSaveToDeviceIntent(writerResults.files.filterNotNull(), trip, receipts, distances)
                    EmailResult.Success(sendIntent)
                }

            }
        }
    }

    private fun prepareSendAttachmentsIntent(
        attachments: List<File>, trip: Trip, receipts: List<Receipt>, distances: List<Distance>
    ): Intent {
        val bodyBuilder = StringBuilder()

        for (attachment in attachments) {
            if (attachment.length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n")
                bodyBuilder.append(context.getString(R.string.email_body_subject_5mb_warning, attachment.absolutePath))
            }
        }

        Logger.info(this, "Built the following files [{}].", attachments)

        var body = bodyBuilder.toString()

        if (body.isNotEmpty()) {
            body = "\n\n" + body
        }

        when {
            attachments.size == 1 -> body = context.getString(R.string.report_attached).toString() + body
            attachments.size > 1 -> body =
                context.getString(R.string.reports_attached, Integer.toString(attachments.size)).toString() + body
        }

        val emailIntent: Intent = intentUtils.getSendIntent(context, attachments)
        val to = preferenceManager.get(UserPreference.Email.ToAddresses).split(";".toRegex()).toTypedArray()
        val cc = preferenceManager.get(UserPreference.Email.CcAddresses).split(";".toRegex()).toTypedArray()
        val bcc = preferenceManager.get(UserPreference.Email.BccAddresses).split(";".toRegex()).toTypedArray()
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        emailIntent.putExtra(Intent.EXTRA_CC, cc)
        emailIntent.putExtra(Intent.EXTRA_BCC, bcc)

        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            SmartReceiptsFormattableString(
                preferenceManager.get(UserPreference.Email.Subject), trip, preferenceManager, dateFormatter, receipts, distances
            ).toString()
        )
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)

        if(Build.VERSION.SDK_INT >= 38) {

            // Todo: Add Intent.EXTRA_EXCLUDE_COMPONENTS to your intent after calling Intent.createChooser():

            val shareIntent = Intent.createChooser(emailIntent, null)
            val customActions = arrayOf(
                ChooserAction.Builder(
                    Icon.createWithResource(context, R.drawable.download_24px),
                    "Save to Downloads",
                    PendingIntent.getBroadcast(
                        context,
                        1,
                        Intent(Intent.ACTION_VIEW),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                    )
                ).build()
            )
            shareIntent.putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, customActions)

            return shareIntent

        }

        Logger.debug(this, "Built the send intent {} with extras {}.", emailIntent, emailIntent.extras)

        return emailIntent
    }

    fun prepareSaveToDeviceIntent(attachments: List<File>, trip: Trip, receipts: List<Receipt>, distances: List<Distance>
    ): Intent {


        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, attachments[0].name)

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        //startActivityForResult(intent, 1234)
        return intent


    }

    fun copyFilesToDevice(files: List<File>, trip: Trip, receipts: List<Receipt>, distances: List<Distance>): List<Uri> {

        val contentResolver = context.contentResolver
        val actualUriPermissions = contentResolver.persistedUriPermissions



        val mostRecentUriPermissionUri = try {
            actualUriPermissions.maxBy { it.persistedTime }
                .takeIf { it?.isWritePermission == true }?.uri
        }
        catch (e: NoSuchElementException) {
                Log.e("uriPersmissions", e.toString())
            throw e
            null
        }

        //val data = files[0].readBytes()
        val fileName = files[0].name
        val mimeType = UriUtils.getMimeType(files[0], context)
        var uriList = mutableListOf<Uri>()

        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            mostRecentUriPermissionUri,
            DocumentsContract.getTreeDocumentId(mostRecentUriPermissionUri)
        )
        try {
            files.forEach { file ->
                val mimeType = UriUtils.getMimeType(file, context)


                val fileUri =
                    DocumentsContract.createDocument(contentResolver, docUri, mimeType, file.name)

                fileUri?.let { contentResolver.openOutputStream(it) }
                    ?.buffered()
                    ?.use { it.write(file.readBytes()) }

                if (fileUri != null) uriList.add(fileUri)
            }
        }
        catch (e: IOException) {
            Log.e("SAF writeFile", e.toString())
        }
        catch (e: FileNotFoundException) {
            Log.e("SAF writeFile", "Could not create document \n$e")

        }

        return uriList
//        try {
//            DocumentFile.fromTreeUri(context, mostRecentUriPermissionUri!!)
//                ?.createFile(mimeType, fileName)
//                ?.let { contentResolver.openOutputStream(it.uri) }
//                ?.buffered()
//                ?.use { it.write(data) }

    }
}


