package co.smartreceipts.android.workers

import android.content.Context
import android.content.Intent
import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.persistence.database.tables.DistanceTable
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.IntentUtils
import co.smartreceipts.android.workers.EmailAssistant.EmailOptions
import co.smartreceipts.android.workers.widget.EmailResult
import co.smartreceipts.android.workers.widget.GenerationErrors
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.*

@RunWith(RobolectricTestRunner::class)
class EmailAssistantTest {

    // Class under test
    private lateinit var emailAssistant: EmailAssistant

    private val context = mock<Context>()
    private val databaseHelper = mock<DatabaseHelper>()
    private val preferenceManager = mock<UserPreferenceManager>()
    private val attachmentFilesWriter = mock<AttachmentFilesWriter>()
    private val dateFormatter = mock<DateFormatter>()
    private val intentUtils = mock<IntentUtils>()

    private val trip = DefaultObjects.newDefaultTrip()
    private val receiptsTable = mock<ReceiptsTable>()
    private val distancesTable = mock<DistanceTable>()

    private val to = "to"
    private val cc = "cc"
    private val bcc = "bcc"
    private val subject = "subject"


    @Before
    fun setUp() {
        whenever(databaseHelper.receiptsTable).thenReturn(receiptsTable)
        whenever(databaseHelper.distanceTable).thenReturn(distancesTable)

        whenever(receiptsTable.get(trip, true)).thenReturn(Single.just(emptyList()))
        whenever(distancesTable.get(trip, true)).thenReturn(Single.just(emptyList()))


        whenever(preferenceManager.get(UserPreference.Email.ToAddresses)).thenReturn(to)
        whenever(preferenceManager.get(UserPreference.Email.CcAddresses)).thenReturn(cc)
        whenever(preferenceManager.get(UserPreference.Email.BccAddresses)).thenReturn(bcc)
        whenever(preferenceManager.get(UserPreference.Email.Subject)).thenReturn(subject)
        whenever(preferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false)
        whenever(preferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false)
        whenever(preferenceManager.get(UserPreference.ReportOutput.UserId)).thenReturn("")

        whenever(context.getString(R.string.report_attached)).thenReturn("1 report attached")
        whenever(context.packageName).thenReturn("")

        whenever(dateFormatter.getFormattedDate(trip.startDisplayableDate)).thenReturn("")
        whenever(dateFormatter.getFormattedDate(trip.endDisplayableDate)).thenReturn("")

        emailAssistant = EmailAssistant(context, databaseHelper, preferenceManager, attachmentFilesWriter, dateFormatter, intentUtils)
    }

    @Test
    fun emailTripErrorNoOptionsTest() {
        val options = EnumSet.noneOf(EmailOptions::class.java)

        val result = emailAssistant.emailTrip(trip, options).blockingGet()

        assert(result is EmailResult.Error && result.errorType == GenerationErrors.ERROR_NO_SELECTION)

        verifyZeroInteractions(databaseHelper, preferenceManager, attachmentFilesWriter, dateFormatter)
    }

    @Test
    fun emailTripErrorNoReceiptsNoDistancesTest() {
        // user wants to create report but there are no receipts and no distances

        val options = EnumSet.allOf(EmailOptions::class.java)

        val result = emailAssistant.emailTrip(trip, options).blockingGet()

        assert(result is EmailResult.Error && result.errorType == GenerationErrors.ERROR_NO_RECEIPTS)

        verify(databaseHelper).receiptsTable
        verify(databaseHelper).distanceTable
        verifyNoMoreInteractions(databaseHelper)
        verifyZeroInteractions(preferenceManager, attachmentFilesWriter, dateFormatter)
    }

    @Test
    fun emailTripErrorNoReceiptsDistancesNotPdfNotCsvTest() {
        // user wants to create report (not PDF or CSV) with just distances

        val options = EnumSet.of(EmailOptions.ZIP, EmailOptions.ZIP_WITH_METADATA, EmailOptions.PDF_IMAGES_ONLY)

        whenever(distancesTable.get(trip, true)).thenReturn(Single.just(listOf(mock())))

        val result = emailAssistant.emailTrip(trip, options).blockingGet()

        assert(result is EmailResult.Error && result.errorType == GenerationErrors.ERROR_NO_RECEIPTS)

        verify(databaseHelper).receiptsTable
        verify(databaseHelper).distanceTable
        verifyNoMoreInteractions(databaseHelper)
        verifyZeroInteractions(preferenceManager, attachmentFilesWriter, dateFormatter)
    }

    @Test
    fun emailTripErrorNoReceiptsDisabledDistancesPdfTest() {
        // user wants to create PDF report with just distances but this option is disabled

        val optionsPdf = EnumSet.of(EmailOptions.PDF_FULL)

        whenever(distancesTable.get(trip, true)).thenReturn(Single.just(listOf(mock())))
        whenever(preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)).thenReturn(false)

        val result = emailAssistant.emailTrip(trip, optionsPdf).blockingGet()

        assert(result is EmailResult.Error && result.errorType == GenerationErrors.ERROR_DISABLED_DISTANCES)

        verify(databaseHelper).receiptsTable
        verify(databaseHelper).distanceTable
        verifyNoMoreInteractions(databaseHelper)
        verify(preferenceManager).get(UserPreference.Distance.PrintDistanceTableInReports)
        verifyNoMoreInteractions(preferenceManager)
        verifyZeroInteractions(attachmentFilesWriter, dateFormatter)
    }

    @Test
    fun emailTripErrorNoReceiptsDisabledDistancesCsvTest() {
        // user wants to create CSV report with just distances but this option is disabled

        val optionsCsv = EnumSet.of(EmailOptions.CSV)

        whenever(distancesTable.get(trip, true)).thenReturn(Single.just(listOf(mock())))
        whenever(preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)).thenReturn(false)

        val result = emailAssistant.emailTrip(trip, optionsCsv).blockingGet()

        assert(result is EmailResult.Error && result.errorType == GenerationErrors.ERROR_DISABLED_DISTANCES)

        verify(databaseHelper).receiptsTable
        verify(databaseHelper).distanceTable
        verifyNoMoreInteractions(databaseHelper)
        verify(preferenceManager).get(UserPreference.Distance.PrintDistanceTableInReports)
        verifyNoMoreInteractions(preferenceManager)
        verifyZeroInteractions(attachmentFilesWriter, dateFormatter)
    }

    @Test
    fun emailTripErrorMemoryTest() {
        val options = EnumSet.allOf(EmailOptions::class.java)
        val distancesList: List<Distance> = emptyList()
        val receiptsList: List<Receipt> = listOf(mock())

        val writerResults = AttachmentFilesWriter.WriterResults()
        writerResults.didMemoryErrorOccure = true

        whenever(receiptsTable.get(trip, true)).thenReturn(Single.just(receiptsList))
        whenever(attachmentFilesWriter.write(trip, receiptsList, distancesList, options)).thenReturn(writerResults)

        val result = emailAssistant.emailTrip(trip, options).blockingGet()

        assert(result is EmailResult.Error && result.errorType == GenerationErrors.ERROR_MEMORY)

        verify(databaseHelper).receiptsTable
        verify(databaseHelper).distanceTable
        verify(attachmentFilesWriter).write(trip, receiptsList, distancesList, options)
        verifyNoMoreInteractions(databaseHelper, attachmentFilesWriter)
    }

    @Test
    fun emailTripErrorTooManyColumns() {
        val options = EnumSet.of(EmailOptions.PDF_FULL)
        val distancesList: List<Distance> = emptyList()
        val receiptsList: List<Receipt> = listOf(mock())

        val writerResults = AttachmentFilesWriter.WriterResults()
        writerResults.didPDFFailCompletely = true
        writerResults.didPDFFailTooManyColumns = true

        whenever(receiptsTable.get(trip, true)).thenReturn(Single.just(receiptsList))
        whenever(attachmentFilesWriter.write(trip, receiptsList, distancesList, options)).thenReturn(writerResults)

        val result = emailAssistant.emailTrip(trip, options).blockingGet()

        assert(result is EmailResult.Error && result.errorType == GenerationErrors.ERROR_TOO_MANY_COLUMNS)

        verify(databaseHelper).receiptsTable
        verify(databaseHelper).distanceTable
        verify(attachmentFilesWriter).write(trip, receiptsList, distancesList, options)
        verifyNoMoreInteractions(databaseHelper, attachmentFilesWriter)
    }

    @Test
    fun emailTripErrorPdfGeneration() {
        val options = EnumSet.of(EmailOptions.PDF_FULL)
        val distancesList: List<Distance> = emptyList()
        val receiptsList: List<Receipt> = listOf(mock())

        val writerResults = AttachmentFilesWriter.WriterResults()
        writerResults.didPDFFailCompletely = true

        whenever(receiptsTable.get(trip, true)).thenReturn(Single.just(receiptsList))
        whenever(attachmentFilesWriter.write(trip, receiptsList, distancesList, options)).thenReturn(writerResults)

        val result = emailAssistant.emailTrip(trip, options).blockingGet()

        assert(result is EmailResult.Error && result.errorType == GenerationErrors.ERROR_PDF_GENERATION)

        verify(databaseHelper).receiptsTable
        verify(databaseHelper).distanceTable
        verify(attachmentFilesWriter).write(trip, receiptsList, distancesList, options)
        verifyNoMoreInteractions(databaseHelper, attachmentFilesWriter)
    }

    @Test
    fun emailTripSuccess() {
        val options = EnumSet.of(EmailOptions.PDF_FULL, EmailOptions.CSV)
        val distancesList: List<Distance> = emptyList()
        val receiptsList: List<Receipt> = listOf(ReceiptBuilderFactory().setTrip(trip).build())
        val filesList: Array<File?> = arrayOf(mock())
        val sendIntent = Intent()
        val writerResults = mock<AttachmentFilesWriter.WriterResults>()

        whenever(writerResults.didPDFFailCompletely).thenReturn(false)
        whenever(writerResults.didMemoryErrorOccure).thenReturn(false)
        whenever(writerResults.files).thenReturn(filesList)

        whenever(receiptsTable.get(trip, true)).thenReturn(Single.just(receiptsList))
        whenever(attachmentFilesWriter.write(trip, receiptsList, distancesList, options)).thenReturn(writerResults)
        whenever(intentUtils.getSendIntent(context, filesList.filterNotNull())).thenReturn(sendIntent)


        val result = emailAssistant.emailTrip(trip, options).blockingGet()
        assert(result is EmailResult.Success)


        val intent = (result as EmailResult.Success).intent

        val ccExtra = intent.getStringArrayExtra(Intent.EXTRA_CC)!!
        assert(ccExtra.size == 1 && ccExtra[0] == cc)

        val bccExtra = intent.getStringArrayExtra(Intent.EXTRA_BCC)!!
        assert(bccExtra.size == 1 && bccExtra[0] == bcc)

        val toExtra = intent.getStringArrayExtra(Intent.EXTRA_EMAIL)!!
        assert(toExtra.size == 1 && toExtra[0] == to)

        val subjectExtra = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        assertEquals(subject, subjectExtra)

        val bodyExtra = intent.getStringExtra(Intent.EXTRA_TEXT)
        assertEquals("1 report attached", bodyExtra)

        verify(databaseHelper).receiptsTable
        verify(databaseHelper).distanceTable
        verify(attachmentFilesWriter).write(trip, receiptsList, distancesList, options)
        verify(intentUtils).getSendIntent(context, filesList.filterNotNull())
    }

}