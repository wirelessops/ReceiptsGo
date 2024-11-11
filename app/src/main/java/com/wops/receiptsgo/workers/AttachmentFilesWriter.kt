package com.wops.receiptsgo.workers

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.graphics.Paint.Align
import com.wops.analytics.log.Logger
import com.wops.receiptsgo.R
import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.filters.LegacyReceiptFilter
import com.wops.receiptsgo.model.*
import com.wops.receiptsgo.model.comparators.ReceiptDateComparator
import com.wops.receiptsgo.model.converters.DistanceToReceiptsConverter
import com.wops.receiptsgo.model.factory.PriceBuilderFactory
import com.wops.receiptsgo.model.impl.columns.categories.CategoryColumnDefinitions
import com.wops.receiptsgo.model.impl.columns.distance.DistanceColumnDefinitions
import com.wops.receiptsgo.persistence.DatabaseHelper
import com.wops.receiptsgo.persistence.database.controllers.grouping.GroupingController
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.workers.EmailAssistant.EmailOptions
import com.wops.receiptsgo.workers.reports.Report
import com.wops.receiptsgo.workers.reports.ReportGenerationException
import com.wops.receiptsgo.workers.reports.ReportResourcesManager
import com.wops.receiptsgo.workers.reports.csv.CsvReportWriter
import com.wops.receiptsgo.workers.reports.csv.CsvTableGenerator
import com.wops.receiptsgo.workers.reports.pdf.PdfBoxFullPdfReport
import com.wops.receiptsgo.workers.reports.pdf.PdfBoxImagesOnlyReport
import com.wops.receiptsgo.workers.reports.pdf.misc.TooManyColumnsException
import com.wops.core.di.scopes.ApplicationScope
import wb.android.storage.StorageManager
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

@ApplicationScope
class AttachmentFilesWriter @Inject constructor(
    private val context: Context,
    private val databaseHelper: DatabaseHelper,
    private val preferenceManager: UserPreferenceManager,
    private val storageManager: StorageManager,
    private val reportResourcesManager: ReportResourcesManager,
    private val purchaseWallet: PurchaseWallet,
    private val dateFormatter: DateFormatter
) {

    companion object {
        private const val IMG_SCALE_FACTOR = 2.1f
        private const val HW_RATIO = 0.75f
    }

    class WriterResults {
        var didPDFFailCompletely = false
        var didPDFFailTooManyColumns = false
        var didCSVFailCompletely = false
        var didZIPFailCompletely = false
        var didMemoryErrorOccure = false

        val files: Array<File?> = arrayOfNulls(EmailOptions.values().size)
    }

    fun write(trip: Trip, receiptsList: List<Receipt>, distancesList: List<Distance>, options: EnumSet<EmailOptions>): WriterResults {
        Logger.info(this, "Generating the following report types {}.", options)

        val results = WriterResults()

        // Make our trip output directory exists in a good state
        var dir = trip.directory
        if (!dir.exists()) {
            dir = storageManager.getFile(trip.name)
            if (!dir.exists()) {
                dir = storageManager.mkdir(trip.name)
            }
        }

        if (options.contains(EmailOptions.PDF_FULL)) {
            generateFullPdf(trip, results)
        }

        if (options.contains(EmailOptions.PDF_IMAGES_ONLY) && receiptsList.isNotEmpty()) {
            generateImagesPdf(trip, results)
        }

        if (options.contains(EmailOptions.ZIP) && receiptsList.isNotEmpty()) {
            generateZip(trip, receiptsList, dir, results)
        }

        val csvColumns by lazy { databaseHelper.csvTable.get().blockingGet() }

        if (options.contains(EmailOptions.CSV)) {
            generateCsv(trip, receiptsList, distancesList, csvColumns, dir, results)
        }

        if (options.contains(EmailOptions.ZIP_WITH_METADATA) && receiptsList.isNotEmpty()) {
            generateZipWithMetadata(trip, receiptsList, csvColumns, dir, options.contains(EmailOptions.ZIP), results)
        }

        return results
    }

    private fun generateFullPdf(trip: Trip, results: WriterResults) {
        val pdfFullReport: Report =
            PdfBoxFullPdfReport(reportResourcesManager, databaseHelper, preferenceManager, storageManager, purchaseWallet, dateFormatter)

        try {
            results.files[EmailOptions.PDF_FULL.index] = pdfFullReport.generate(trip)
        } catch (e: ReportGenerationException) {
            if (e.cause is TooManyColumnsException) {
                results.didPDFFailTooManyColumns = true
            }
            results.didPDFFailCompletely = true
        }
    }

    private fun generateImagesPdf(trip: Trip, results: WriterResults) {
        val pdfImagesReport: Report =
            PdfBoxImagesOnlyReport(reportResourcesManager, databaseHelper, preferenceManager, storageManager, dateFormatter)

        try {
            results.files[EmailOptions.PDF_IMAGES_ONLY.index] = pdfImagesReport.generate(trip)
        } catch (e: ReportGenerationException) {
            results.didPDFFailCompletely = true
        }
    }

    private fun generateCsv(
        trip: Trip, receiptsList: List<Receipt>, distancesList: List<Distance>, csvColumns: List<Column<Receipt>>, dir: File,
        results: WriterResults
    ) {
        val printFooters: Boolean = preferenceManager.get(UserPreference.ReportOutput.ShowTotalOnCSV)

        try {
            storageManager.delete(dir, dir.name + ".csv")

            val csvTableGenerator =
                CsvTableGenerator(reportResourcesManager, csvColumns, true, printFooters, LegacyReceiptFilter(preferenceManager))

            val receipts: MutableList<Receipt> = ArrayList(receiptsList)
            val distances: MutableList<Distance> = ArrayList(distancesList)

            // Receipts table
            if (preferenceManager.get(UserPreference.Distance.PrintDistanceAsDailyReceiptInReports)) {
                receipts.addAll(DistanceToReceiptsConverter(context, dateFormatter).convert(distances))
                Collections.sort(receipts, ReceiptDateComparator())
            }

            var data = csvTableGenerator.generate(receipts)

            // Distance table
            if (preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)) {
                if (distances.isNotEmpty()) {
                    distances.reverse() // Reverse the list, so we print the most recent one first

                    // CSVs cannot print special characters
                    val distanceColumnDefinitions: ColumnDefinitions<Distance> =
                        DistanceColumnDefinitions(reportResourcesManager, preferenceManager, dateFormatter, true)
                    val distanceColumns = distanceColumnDefinitions.allColumns
                    data += "\n\n"
                    data += CsvTableGenerator(
                        reportResourcesManager, distanceColumns,
                        true, printFooters
                    ).generate(distances)
                }
            }

            // Categorical summation table
            if (preferenceManager.get(UserPreference.PlusSubscription.CategoricalSummationInReports)) {
                val sumCategoryGroupingResults = GroupingController(databaseHelper, context, preferenceManager)
                    .getSummationByCategory(trip)
                    .toList()
                    .blockingGet()
                var isMultiCurrency = false
                for (sumCategoryGroupingResult in sumCategoryGroupingResults) {
                    if (sumCategoryGroupingResult.isMultiCurrency) {
                        isMultiCurrency = true
                        break
                    }
                }
                val taxEnabled: Boolean = preferenceManager.get(UserPreference.Receipts.IncludeTaxField)
                val categoryColumns = CategoryColumnDefinitions(reportResourcesManager, isMultiCurrency, taxEnabled)
                    .allColumns
                data += "\n\n"
                data += CsvTableGenerator(
                    reportResourcesManager, categoryColumns,
                    true, printFooters
                ).generate(sumCategoryGroupingResults)
            }

            // Separated tables for each category
            if (preferenceManager.get(UserPreference.PlusSubscription.SeparateByCategoryInReports)) {
                val groupingResults = GroupingController(databaseHelper, context, preferenceManager)
                    .getReceiptsGroupedByCategory(trip)
                    .toList()
                    .blockingGet()
                for (groupingResult in groupingResults) {
                    data += "\n\n" + groupingResult.category.name + "\n";
                    data += CsvTableGenerator(reportResourcesManager, csvColumns, true, printFooters).generate(groupingResult.receipts)
                }
            }

            val csvFile = File(dir, dir.name + ".csv")
            results.files[EmailOptions.CSV.index] = csvFile
            CsvReportWriter(csvFile).write(data)

        } catch (e: IOException) {
            Logger.error(this, "Failed to write the csv file", e)
            results.didCSVFailCompletely = true;
        }
    }

    private fun generateZip(trip: Trip, receiptsList: List<Receipt>, directory: File, results: WriterResults) {
        var dir = directory
        storageManager.delete(dir, dir.name + ".zip")
        dir = storageManager.mkdir(trip.directory, trip.name)
        for (i in receiptsList.indices) {
            val receipt = receiptsList[i]
            if (!filterOutReceipt(preferenceManager, receipt) && receipt.file != null && receipt.file.exists()) {
                val data = storageManager.read(receipt.file)
                if (data != null) storageManager.write(dir, receipt.file.name, data)
            }
        }
        val zip: File = storageManager.zipBuffered(dir, 2048)
        storageManager.deleteRecursively(dir)
        results.files[EmailOptions.ZIP.index] = zip
    }

    private fun generateZipWithMetadata(
        trip: Trip, receiptsList: List<Receipt>, csvColumns: List<Column<Receipt>>,
        dir: File, isZipGenerationIncluded: Boolean, results: WriterResults
    ) {
        val zipDir = if (isZipGenerationIncluded) {
            storageManager.delete(dir, dir.name + "_stamped" + ".zip")
            storageManager.mkdir(trip.directory, trip.name + "_stamped")
        } else {
            storageManager.delete(dir, dir.name + ".zip")
            storageManager.mkdir(trip.directory, trip.name)
        }
        for (i in receiptsList.indices) {
            val receipt = receiptsList[i]
            if (!filterOutReceipt(preferenceManager, receipt)) {
                if (receipt.hasImage()) {
                    val userCommentBuilder = StringBuilder()
                    for (col in csvColumns) {
                        userCommentBuilder.append(reportResourcesManager.getFlexString(col.headerStringResId))
                        userCommentBuilder.append(": ")
                        userCommentBuilder.append(col.getValue(receipt))
                        userCommentBuilder.append("\n")
                    }
                    val userComment = userCommentBuilder.toString()
                    try {
                        val b: Bitmap? = stampImage(trip, receipt, Bitmap.Config.ARGB_8888)
                        if (b != null) {
                            storageManager.writeBitmap(zipDir, b, receipt.file!!.name, CompressFormat.JPEG, 85, userComment)
                            b.recycle()
                        }
                    } catch (e: OutOfMemoryError) {
                        Logger.error(this, "Trying to recover from OOM", e)
                        System.gc()
                        try {
                            val b: Bitmap? = stampImage(trip, receipt, Bitmap.Config.RGB_565)
                            if (b != null) {
                                storageManager.writeBitmap(zipDir, b, receipt.file!!.name, CompressFormat.JPEG, 85, userComment)
                                b.recycle()
                            }
                        } catch (e2: OutOfMemoryError) {
                            Logger.error(this, "Failed to recover from OOM", e2)
                            results.didZIPFailCompletely = true
                            results.didMemoryErrorOccure = true
                            break
                        }
                    }
                } else if (receipt.hasPDF()) {
                    val data = storageManager.read(receipt.file)
                    if (data != null) storageManager.write(zipDir, receipt.file!!.name, data)
                }
            }
        }
        val zipWithMetadata: File = storageManager.zipBuffered(zipDir, 2048)
        storageManager.deleteRecursively(zipDir)
        results.files[EmailOptions.ZIP_WITH_METADATA.index] = zipWithMetadata
    }

    /**
     * Applies a particular filter to determine whether or not this receipt should be
     * generated for this report
     *
     * @param preferences - User preferences
     * @param receipt     - The particular receipt
     * @return true if if should be filtered out, false otherwise
     */
    private fun filterOutReceipt(preferences: UserPreferenceManager, receipt: Receipt): Boolean {
        return if (preferences.get(UserPreference.Receipts.OnlyIncludeReimbursable) && !receipt.isReimbursable) {
            true
        } else receipt.price.priceAsFloat < preferences.get(UserPreference.Receipts.MinimumReceiptPrice)
    }

    private fun stampImage(trip: Trip, receipt: Receipt, config: Bitmap.Config): Bitmap? {
        if (!receipt.hasImage()) {
            return null
        }
        var foreground: Bitmap? = storageManager.getMutableMemoryEfficientBitmap(receipt.file)
        return if (foreground != null) { // It can be null if file not found
            // Size the image
            var foreWidth = foreground.width
            var foreHeight = foreground.height
            if (foreHeight > foreWidth) {
                foreWidth = (foreHeight * HW_RATIO).toInt()
            } else {
                foreHeight = (foreWidth / HW_RATIO).toInt()
            }

            // Set up the padding
            val xPad = (foreWidth / IMG_SCALE_FACTOR).toInt()
            val yPad = (foreHeight / IMG_SCALE_FACTOR).toInt()

            // Set up an all white background for our canvas
            val background = Bitmap.createBitmap(foreWidth + xPad, foreHeight + yPad, config)
            val canvas = Canvas(background)
            canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF) //This represents White color

            // Set up the paint
            val dither = Paint()
            dither.isDither = true
            dither.isFilterBitmap = false
            canvas.drawBitmap(
                foreground,
                (background.width - foreground.width) / 2.toFloat(),
                (background.height - foreground.height) / 2.toFloat(),
                dither
            )
            val brush = Paint()
            brush.isAntiAlias = true
            brush.typeface = Typeface.SANS_SERIF
            brush.color = Color.BLACK
            brush.style = Paint.Style.FILL
            brush.textAlign = Align.LEFT

            // Set up the number of items to draw
            var num = 5
            if (preferenceManager.get(UserPreference.Receipts.IncludeTaxField)) {
                num++
            }
            if (receipt.hasExtraEditText1()) {
                num++
            }
            if (receipt.hasExtraEditText2()) {
                num++
            }
            if (receipt.hasExtraEditText3()) {
                num++
            }
            val spacing: Float = getOptimalSpacing(num, yPad / 2, brush)
            var y = spacing * 4
            canvas.drawText(trip.name, xPad / 2.toFloat(), y, brush)
            y += spacing
            canvas.drawText(
                dateFormatter.getFormattedDate(trip.startDisplayableDate) + " -- " + dateFormatter.getFormattedDate(trip.endDisplayableDate),
                xPad / 2.toFloat(),
                y,
                brush
            )
            y = background.height - yPad / 2 + spacing * 2
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_NAME) + ": " + receipt.name,
                xPad / 2.toFloat(),
                y,
                brush
            )
            y += spacing
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_PRICE) + ": " + receipt.price.decimalFormattedPrice + " " + receipt.price.currencyCode,
                xPad / 2.toFloat(),
                y,
                brush
            )
            y += spacing
            if (preferenceManager.get(UserPreference.Receipts.IncludeTaxField)) {
                val totalTax = PriceBuilderFactory(receipt.tax).setPrice(receipt.tax.price.add(receipt.tax2.price)).build()
                canvas.drawText(
                    reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_TAX) + ": " + totalTax.decimalFormattedPrice + " " + receipt.price.currencyCode,
                    xPad / 2.toFloat(),
                    y,
                    brush
                )
                y += spacing
            }
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_DATE) + ": " + dateFormatter.getFormattedDate(
                    receipt.date,
                    receipt.timeZone
                ), xPad / 2.toFloat(), y, brush
            )
            y += spacing
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_CATEGORY) + ": " + receipt.category.name,
                xPad / 2.toFloat(),
                y,
                brush
            )
            y += spacing
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_COMMENT) + ": " + receipt.comment,
                xPad / 2.toFloat(),
                y,
                brush
            )
            y += spacing
            if (receipt.hasExtraEditText1()) {
                canvas.drawText(
                    reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1) + ": " + receipt.extraEditText1,
                    xPad / 2.toFloat(),
                    y,
                    brush
                )
                y += spacing
            }
            if (receipt.hasExtraEditText2()) {
                canvas.drawText(
                    reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2) + ": " + receipt.extraEditText2,
                    xPad / 2.toFloat(),
                    y,
                    brush
                )
                y += spacing
            }
            if (receipt.hasExtraEditText3()) {
                canvas.drawText(
                    reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3) + ": " + receipt.extraEditText3,
                    xPad / 2.toFloat(),
                    y,
                    brush
                )
            }

            // Clear out the dead data here
            foreground.recycle()
            // And return
            background
        } else {
            null
        }
    }

    private fun getOptimalSpacing(count: Int, space: Int, brush: Paint): Float {
        var fontSize = 8f //Seed
        brush.textSize = fontSize
        while (space > (count + 2) * brush.fontSpacing) {
            brush.textSize = ++fontSize
        }
        brush.textSize = --fontSize
        return brush.fontSpacing
    }

}