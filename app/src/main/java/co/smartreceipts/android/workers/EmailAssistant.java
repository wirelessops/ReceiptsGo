package co.smartreceipts.android.workers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.impl.columns.categories.CategoryColumnDefinitions;
import co.smartreceipts.android.model.impl.columns.distance.DistanceColumnDefinitions;
import co.smartreceipts.android.model.impl.columns.receipts.CategoryGroupingReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.grouping.GroupingController;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.Report;
import co.smartreceipts.android.workers.reports.ReportGenerationException;
import co.smartreceipts.android.workers.reports.csv.CsvReportWriter;
import co.smartreceipts.android.workers.reports.csv.CsvTableGenerator;
import co.smartreceipts.android.workers.reports.formatting.SmartReceiptsFormattableString;
import co.smartreceipts.android.workers.reports.pdf.PdfBoxFullPdfReport;
import co.smartreceipts.android.workers.reports.pdf.PdfBoxImagesOnlyReport;
import co.smartreceipts.android.workers.reports.pdf.misc.TooManyColumnsException;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

//TODO: Redo this class... Really sloppy
public class EmailAssistant {

    private static final String DEVELOPER_EMAIL = "will.r.b" + "aumann" + "@" + "gm" + "ail" + "." + "com";

    public enum EmailOptions {
        PDF_FULL(0),
        PDF_IMAGES_ONLY(1),
        CSV(2),
        ZIP_IMAGES_STAMPED(3);

        private final int index;

        EmailOptions(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

    private final Context context;
    private final NavigationHandler navigationHandler;
    private final Flex flex;
    private final PersistenceManager persistenceManager;
    private final Trip trip;

    public static final Intent getEmailDeveloperIntent() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        setEmailDeveloperRecipient(intent);
        intent.setData(Uri.parse("mailto:" + DEVELOPER_EMAIL));
        return intent;
    }

    private static void setEmailDeveloperRecipient(Intent intent) {
        intent.setData(Uri.parse("mailto:" + DEVELOPER_EMAIL));
    }

    public static final Intent getEmailDeveloperIntent(String subject) {
        Intent intent = getEmailDeveloperIntent();
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        return intent;
    }


    public static final Intent getEmailDeveloperIntent(String subject, String body) {
        Intent intent = getEmailDeveloperIntent(subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    public static final Intent getEmailDeveloperIntent(Context context, String subject, String body, List<File> files) {
        Intent intent = IntentUtils.getSendIntent(context, files);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{DEVELOPER_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    public EmailAssistant(NavigationHandler navigationHandler, Context context, Flex flex, PersistenceManager persistenceManager, Trip trip) {
        this.navigationHandler = navigationHandler;
        this.context = context;
        this.flex = flex;
        this.persistenceManager = persistenceManager;
        this.trip = trip;
    }

    public void emailTrip(@NonNull EnumSet<EmailOptions> options) {
        Logger.info(this, "Creating reports...");
        ProgressDialog progress = ProgressDialog.show(context, "", "Building Reports...", true, false);
        EmailAttachmentWriter attachmentWriter = new EmailAttachmentWriter(persistenceManager, progress, options);
        attachmentWriter.execute(trip);
    }

    public void onAttachmentsCreated(File[] attachments) {
        List<File> files = new ArrayList<>();
        StringBuilder bodyBuilder = new StringBuilder();
        String path = "";
        if (attachments[EmailOptions.PDF_FULL.getIndex()] != null) {
            path = attachments[EmailOptions.PDF_FULL.getIndex()].getParentFile().getAbsolutePath();
            files.add(attachments[EmailOptions.PDF_FULL.getIndex()]);
            if (attachments[EmailOptions.PDF_FULL.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(context.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.PDF_FULL.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()] != null) {
            path = attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].getParentFile().getAbsolutePath();
            files.add(attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()]);
            if (attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(context.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.CSV.getIndex()] != null) {
            path = attachments[EmailOptions.CSV.getIndex()].getParentFile().getAbsolutePath();
            files.add(attachments[EmailOptions.CSV.getIndex()]);
            if (attachments[EmailOptions.CSV.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(context.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.CSV.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] != null) {
            path = attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].getParentFile().getAbsolutePath();
            files.add(attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()]);
            if (attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(context.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].getAbsolutePath()));
            }
        }
        Logger.info(this, "Built the following files [{}].", files);

        String body = bodyBuilder.toString();
        if (body.length() > 0) {
            body = "\n\n" + body;
        }
        if (files.size() == 1) {
            body = context.getString(R.string.report_attached) + body;
        } else if (files.size() > 1) {
            body = context.getString(R.string.reports_attached, Integer.toString(files.size())) + body;
        }

        final Intent emailIntent = IntentUtils.getSendIntent(context, files);
        final String[] to = persistenceManager.getPreferenceManager().get(UserPreference.Email.ToAddresses).split(";");
        final String[] cc = persistenceManager.getPreferenceManager().get(UserPreference.Email.CcAddresses).split(";");
        final String[] bcc = persistenceManager.getPreferenceManager().get(UserPreference.Email.BccAddresses).split(";");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_CC, cc);
        emailIntent.putExtra(Intent.EXTRA_BCC, bcc);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, new SmartReceiptsFormattableString(persistenceManager.getPreferenceManager().get(UserPreference.Email.Subject), context, trip, persistenceManager.getPreferenceManager()).toString());
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        Logger.debug(this, "Built the send intent {} with extras {}.", emailIntent, emailIntent.getExtras());

        try {
            context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_email)));
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.error_no_send_intent_dialog_title)
                    .setMessage(context.getString(R.string.error_no_send_intent_dialog_message, path))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    public static final class WriterResults {
        public boolean didPDFFailCompletely = false;
        public boolean didPDFFailPartially = false;
        public boolean didPDFFailTooManyColumns = false;
        public boolean didSimplePDFFailCompletely = false;
        public boolean didSimplePDFFailPartially = false;
        public boolean didCSVFailCompletely = false;
        public boolean didCSVFailPartially = false;
        public boolean didZIPFailCompletely = false;
        public boolean didZIPFailPartially = false;

        public static final WriterResults getFullFailureInstance() {
            WriterResults result = new WriterResults();
            result.didPDFFailCompletely = true;
            result.didPDFFailPartially = true;
            result.didPDFFailTooManyColumns = true;
            result.didSimplePDFFailCompletely = true;
            result.didSimplePDFFailPartially = true;
            result.didCSVFailCompletely = true;
            result.didCSVFailPartially = true;
            result.didZIPFailCompletely = true;
            result.didZIPFailPartially = true;
            return result;
        }
    }

    private class EmailAttachmentWriter extends AsyncTask<Trip, Integer, WriterResults> {

        private final StorageManager mStorageManager;
        private final DatabaseHelper mDB;
        private final UserPreferenceManager mPreferenceManager;
        private final WeakReference<ProgressDialog> mProgressDialog;
        private final File[] mFiles;
        private final EnumSet<EmailOptions> mOptions;
        private boolean memoryErrorOccured = false;

        public EmailAttachmentWriter(PersistenceManager persistenceManager,
                                     ProgressDialog dialog,
                                     EnumSet<EmailOptions> options) {
            mStorageManager = persistenceManager.getStorageManager();
            mDB = persistenceManager.getDatabase();
            mPreferenceManager = persistenceManager.getPreferenceManager();
            mProgressDialog = new WeakReference<>(dialog);
            mOptions = options;
            mFiles = new File[]{null, null, null, null};
            memoryErrorOccured = false;
        }

        @Override
        // TODO: Add all close(s) in finally statements
        protected WriterResults doInBackground(Trip... trips) {
            if (trips.length == 0) {
                return WriterResults.getFullFailureInstance(); //Should never be reached
            }

            // Set up our initial variables
            final Trip trip = trips[0];
            final List<Receipt> receipts = mDB.getReceiptsTable().getBlocking(trip, false);
            final int len = receipts.size();
            final WriterResults results = new WriterResults();

            // Make our trip output directory exists in a good state
            File dir = trip.getDirectory();
            if (!dir.exists()) {
                dir = mStorageManager.getFile(trip.getName());
                if (!dir.exists()) {
                    dir = mStorageManager.mkdir(trip.getName());
                }
            }

            Logger.info(this, "Generating the following report types {}.", mOptions);
            if (mOptions.contains(EmailOptions.PDF_FULL)) {
                final Report pdfFullReport = new PdfBoxFullPdfReport(context, mDB,
                        persistenceManager.getPreferenceManager(), persistenceManager.getStorageManager(),
                        flex);
                try {
                    mFiles[EmailOptions.PDF_FULL.getIndex()] = pdfFullReport.generate(trip);
                } catch (ReportGenerationException e) {
                    if (e.getCause() instanceof TooManyColumnsException) {
                        results.didPDFFailTooManyColumns = true;
                    }
                    results.didPDFFailCompletely = true;
                }
            }
            if (mOptions.contains(EmailOptions.PDF_IMAGES_ONLY)) {
                final Report pdfimagesReport = new PdfBoxImagesOnlyReport(context, persistenceManager, flex);
                try {
                    mFiles[EmailOptions.PDF_IMAGES_ONLY.getIndex()] = pdfimagesReport.generate(trip);
                } catch (ReportGenerationException e) {
                    results.didPDFFailCompletely = true;
                }
            }
            if (mOptions.contains(EmailOptions.CSV)) {
                mStorageManager.delete(dir, dir.getName() + ".csv");

                final List<Column<Receipt>> csvColumns = mDB.getCSVTable().get().blockingGet();
                final CsvTableGenerator<Receipt> csvTableGenerator = new CsvTableGenerator<Receipt>(csvColumns, new LegacyReceiptFilter(mPreferenceManager), true, false);

                String data = csvTableGenerator.generate(receipts);


                // Distance table
                if (mPreferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)) {
                    final List<Distance> distances = new ArrayList<>(mDB.getDistanceTable().getBlocking(trip, false));
                    if (!distances.isEmpty()) {
                        Collections.reverse(distances); // Reverse the list, so we print the most recent one first

                        // CSVs cannot print special characters
                        final ColumnDefinitions<Distance> distanceColumnDefinitions = new DistanceColumnDefinitions(context, mDB, mPreferenceManager, flex, true);
                        final List<Column<Distance>> distanceColumns = distanceColumnDefinitions.getAllColumns();
                        data += "\n\n";
                        data += new CsvTableGenerator<>(distanceColumns, true, true).generate(distances);
                    }
                }

                // Categorical summation table
                if (mPreferenceManager.get(UserPreference.PlusSubscription.CategoricalSummationInReports)) {
                    final List<SumCategoryGroupingResult> sumCategoryGroupingResults = new GroupingController(mDB, context, mPreferenceManager)
                            .getSummationByCategory(trip)
                            .toList()
                            .blockingGet();

                    final List<Column<SumCategoryGroupingResult>> categoryColumns = new CategoryColumnDefinitions(context)
                            .getAllColumns();

                    data += "\n\n";
                    data += new CsvTableGenerator<>(categoryColumns, true, true).generate(sumCategoryGroupingResults);
                }

                // Separated tables for each category
                if (mPreferenceManager.get(UserPreference.PlusSubscription.SeparateByCategoryInReports)) {
                    List<CategoryGroupingResult> groupingResults = new GroupingController(mDB, context, mPreferenceManager)
                            .getReceiptsGroupedByCategory(trip)
                            .toList()
                            .blockingGet();

                    final List<Column<Receipt>> groupingColumns = new CategoryGroupingReceiptColumnDefinitions(context, mPreferenceManager)
                            .getAllColumns();

                    for (CategoryGroupingResult groupingResult : groupingResults) {
                        data += "\n\n";
                        data += groupingResult.getCategory().getName() + "\n";
                        data += new CsvTableGenerator<>(groupingColumns, true, true).generate(groupingResult.getReceipts());
                    }
                }

                String filename = dir.getName() + ".csv";
                File csvFile = new File(dir, filename);

                try {
                    mFiles[EmailOptions.CSV.getIndex()] = csvFile;
                    new CsvReportWriter(csvFile).write(data);
                } catch (IOException e) {
                    Logger.error(this, "Failed to write the csv file", e);
                    results.didCSVFailCompletely = true;
                }
            }
            if (mOptions.contains(EmailOptions.ZIP_IMAGES_STAMPED)) {
                mStorageManager.delete(dir, dir.getName() + ".zip");
                dir = mStorageManager.mkdir(trip.getDirectory(), trip.getName());
                for (int i = 0; i < len; i++) {
                    if (!filterOutReceipt(mPreferenceManager, receipts.get(i)) && receipts.get(i).hasImage()) {
                        try {
                            Bitmap b = stampImage(trip, receipts.get(i), Bitmap.Config.ARGB_8888);
                            if (b != null) {
                                mStorageManager.writeBitmap(dir, b, receipts.get(i).getImage().getName(), CompressFormat.JPEG, 85);
                                b.recycle();
                                b = null;
                            }
                        } catch (OutOfMemoryError e) {
                            Logger.error(this, "Trying to recover from OOM", e);
                            System.gc();
                            try {
                                Bitmap b = stampImage(trip, receipts.get(i), Bitmap.Config.RGB_565);
                                if (b != null) {
                                    mStorageManager.writeBitmap(dir, b, receipts.get(i).getImage().getName(), CompressFormat.JPEG, 85);
                                    b.recycle();
                                }
                            } catch (OutOfMemoryError e2) {
                                Logger.error(this, "Failed to recover from OOM", e2);
                                results.didZIPFailCompletely = true;
                                memoryErrorOccured = true;
                                break;
                            }
                        }
                    }
                }
                File zip = mStorageManager.zipBuffered(dir, 2048);
                mStorageManager.deleteRecursively(dir);
                mFiles[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] = zip;
            }
            return results;
        }

        /**
         * Applies a particular filter to determine whether or not this receipt should be
         * generated for this report
         *
         * @param preferences - User preferences
         * @param receipt     - The particular receipt
         * @return true if if should be filtered out, false otherwise
         */
        private boolean filterOutReceipt(UserPreferenceManager preferences, Receipt receipt) {
            if (preferences.get(UserPreference.Receipts.OnlyIncludeReimbursable) && !receipt.isReimbursable()) {
                return true;
            } else if (receipt.getPrice().getPriceAsFloat() < preferences.get(UserPreference.Receipts.MinimumReceiptPrice)) {
                return true;
            } else {
                return false;
            }
        }

        private static final float IMG_SCALE_FACTOR = 2.1f;
        private static final float HW_RATIO = 0.75f;

        private Bitmap stampImage(final Trip trip, final Receipt receipt, Bitmap.Config config) {
            if (!receipt.hasImage()) {
                return null;
            }
            Bitmap foreground = mStorageManager.getMutableMemoryEfficientBitmap(receipt.getImage());
            if (foreground != null) { // It can be null if file not found
                // Size the image
                int foreWidth = foreground.getWidth();
                int foreHeight = foreground.getHeight();
                if (foreHeight > foreWidth) {
                    foreWidth = (int) (foreHeight * HW_RATIO);
                } else {
                    foreHeight = (int) (foreWidth / HW_RATIO);
                }

                // Set up the paddings
                int xPad = (int) (foreWidth / IMG_SCALE_FACTOR);
                int yPad = (int) (foreHeight / IMG_SCALE_FACTOR);

                // Set up an all white background for our canvas
                Bitmap background = Bitmap.createBitmap(foreWidth + xPad, foreHeight + yPad, config);
                Canvas canvas = new Canvas(background);
                canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF); //This represents White color

                // Set up the paint
                Paint dither = new Paint();
                dither.setDither(true);
                dither.setFilterBitmap(false);
                canvas.drawBitmap(foreground, (background.getWidth() - foreground.getWidth()) / 2, (background.getHeight() - foreground.getHeight()) / 2, dither);
                Paint brush = new Paint();
                brush.setAntiAlias(true);
                brush.setTypeface(Typeface.SANS_SERIF);
                brush.setColor(Color.BLACK);
                brush.setStyle(Paint.Style.FILL);
                brush.setTextAlign(Align.LEFT);

                // Set up the number of items to draw
                int num = 5;
                if (mPreferenceManager.get(UserPreference.Receipts.IncludeTaxField)) {
                    num++;
                }
                if (receipt.hasExtraEditText1()) {
                    num++;
                }
                if (receipt.hasExtraEditText2()) {
                    num++;
                }
                if (receipt.hasExtraEditText3()) {
                    num++;
                }
                float spacing = getOptimalSpacing(num, yPad / 2, brush);
                float y = spacing * 4;
                canvas.drawText(trip.getName(), xPad / 2, y, brush);
                y += spacing;
                canvas.drawText(trip.getFormattedStartDate(context, persistenceManager.getPreferenceManager().get(UserPreference.General.DateSeparator)) + " -- " + trip.getFormattedEndDate(context, persistenceManager.getPreferenceManager().get(UserPreference.General.DateSeparator)), xPad / 2, y, brush);
                y += spacing;
                y = background.getHeight() - yPad / 2 + spacing * 2;
                canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_NAME) + ": " + receipt.getName(), xPad / 2, y, brush);
                y += spacing;
                canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_PRICE) + ": " + receipt.getPrice().getDecimalFormattedPrice() + " " + receipt.getPrice().getCurrencyCode(), xPad / 2, y, brush);
                y += spacing;
                if (mPreferenceManager.get(UserPreference.Receipts.IncludeTaxField)) {
                    canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_TAX) + ": " + receipt.getTax().getDecimalFormattedPrice() + " " + receipt.getPrice().getCurrencyCode(), xPad / 2, y, brush);
                    y += spacing;
                }
                canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_DATE) + ": " + receipt.getFormattedDate(context, persistenceManager.getPreferenceManager().get(UserPreference.General.DateSeparator)), xPad / 2, y, brush);
                y += spacing;
                canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_CATEGORY) + ": " + receipt.getCategory().getName(), xPad / 2, y, brush);
                y += spacing;
                canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_COMMENT) + ": " + receipt.getComment(), xPad / 2, y, brush);
                y += spacing;
                if (receipt.hasExtraEditText1()) {
                    canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1) + ": " + receipt.getExtraEditText1(), xPad / 2, y, brush);
                    y += spacing;
                }
                if (receipt.hasExtraEditText2()) {
                    canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2) + ": " + receipt.getExtraEditText2(), xPad / 2, y, brush);
                    y += spacing;
                }
                if (receipt.hasExtraEditText3()) {
                    canvas.drawText(flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3) + ": " + receipt.getExtraEditText3(), xPad / 2, y, brush);
                    y += spacing;
                }

                // Clear out the dead data here
                foreground.recycle();
                foreground = null;

                // And return
                return background;
            } else {
                return null;
            }
        }

        private float getOptimalSpacing(int count, int space, Paint brush) {
            float fontSize = 8f; //Seed
            brush.setTextSize(fontSize);
            while (space > (count + 2) * brush.getFontSpacing()) {
                brush.setTextSize(++fontSize);
            }
            brush.setTextSize(--fontSize);
            return brush.getFontSpacing();
        }


        @Override
        protected void onPostExecute(WriterResults result) {
            ProgressDialog dialog = mProgressDialog.get();

            //TODO: Check the other properties of result if necessary...
            if (result.didPDFFailCompletely) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (result.didPDFFailTooManyColumns) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.report_pdf_error_too_many_columns_title)
                            .setMessage(
                                    mPreferenceManager.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)
                                            ? context.getString(R.string.report_pdf_error_too_many_columns_message)
                                            : context.getString(R.string.report_pdf_error_too_many_columns_message_landscape))
                            .setPositiveButton(R.string.report_pdf_error_go_to_settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    navigationHandler.navigateToSettingsScrollToReportSection();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();

                } else {
                    Toast.makeText(context, R.string.report_pdf_generation_error, Toast.LENGTH_SHORT).show();
                }
            } else {

                EmailAssistant.this.onAttachmentsCreated(mFiles);
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (memoryErrorOccured) {
                memoryErrorOccured = false;
                Toast.makeText(context, "Error: Not enough memory to stamp the images. Try stopping some other apps and try again.", Toast.LENGTH_LONG).show();
            }
        }

    }

}