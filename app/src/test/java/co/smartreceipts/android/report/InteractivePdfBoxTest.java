package co.smartreceipts.android.report;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.TestResourceReader;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.impl.columns.categories.CategoryCodeColumn;
import co.smartreceipts.android.model.impl.columns.categories.CategoryCurrencyColumn;
import co.smartreceipts.android.model.impl.columns.categories.CategoryNameColumn;
import co.smartreceipts.android.model.impl.columns.categories.CategoryPriceColumn;
import co.smartreceipts.android.model.impl.columns.categories.CategoryTaxColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceCommentColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceCurrencyColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceDateColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceDistanceColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceLocationColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistancePriceColumn;
import co.smartreceipts.android.model.impl.columns.distance.DistanceRateColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptDateColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPriceColumn;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.utils.ReceiptUtils;
import co.smartreceipts.android.utils.TripUtils;
import co.smartreceipts.android.utils.shadows.ShadowFontFileFinder;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import co.smartreceipts.android.workers.reports.pdf.renderer.text.FallbackTextRenderer;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * This contains a series of PDF generation related tests to enable us to evaluate our integration
 * with the PdfBox stack. This class is designed to be both interactive (allowing us to view the
 * resultant files) and automated. Automated tests are traditional unit tests, which will allow us
 * to quickly confirm that everything is operating as expected from a high-level perspective.
 * <p>
 * Should you be interested in the actual PDF generation results, please temporarily remove the
 * {@link After} annotation from the {@link #tearDown()} method that deletes the resultant file
 * </p>
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowFontFileFinder.class})
public class InteractivePdfBoxTest {

    Context context;

    TestResourceReader testResourceReader;

    File outputFile = new File("report.pdf");

    @Mock
    PersistenceManager persistenceManager;

    @Mock
    UserPreferenceManager userPreferenceManager;

    /**
     * Base method, to be overridden by subclasses. The subclass must annotate the method
     * with the JUnit <code>@Before</code> annotation, and initialize the mocks.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        context = RuntimeEnvironment.application;
        testResourceReader = new TestResourceReader();

        when(persistenceManager.getPreferenceManager()).thenReturn(userPreferenceManager);

        when(userPreferenceManager.get(UserPreference.General.DateSeparator)).thenReturn("/");
        when(userPreferenceManager.get(UserPreference.General.IncludeCostCenter)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.ReportOutput.PrintUserIdByPdfPhoto)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.ReportOutput.UserId)).thenReturn("");
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.MinimumReceiptPrice)).thenReturn(-Float.MAX_VALUE);
        when(userPreferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Distance.PrintDistanceAsDailyReceiptInReports)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("Report generated using Smart Receipts for Android");

        FallbackTextRenderer.setHeightMeasureSpec(View.MeasureSpec.makeMeasureSpec(25, View.MeasureSpec.EXACTLY));
    }

    @After
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void tearDown() {
        FallbackTextRenderer.resetHeightMeasureSpec();
        if (outputFile.exists()) {
           outputFile.delete();
        }
    }

    @Test
    public void createReportWithNonPrintableCharacters() throws Exception {

        // Configure test data
        final int count = 1;
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final ReceiptBuilderFactory factory = ReceiptUtils.newDefaultReceiptBuilderFactory(context);
        factory.setFile(imgFile);
        factory.setIsFullPage(true);
        factory.setName("name\n\twith\r\nnon-printable\tchars");
        when(userPreferenceManager.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("footer\n\twith\r\nnon-printable\tchars");

        // Write the file
        writeFullReport(TripUtils.newDefaultTrip(), Collections.singletonList(factory.build()));

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(2, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createReportWithNonWesternCurrencies() throws Exception {

        // Configure test data
        final int count = 1;
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final ReceiptBuilderFactory factory = ReceiptUtils.newDefaultReceiptBuilderFactory(context);
        factory.setFile(imgFile);
        factory.setIsFullPage(true);
        factory.setName("Name with Various Currencies: $£€\u20A3\u20A4\u20A6\u20A7\u20A8\u20A9\u20AA\u20AB\u20AC\u20B1\u20B9\u20BA\u20BC\u20BD.");
        when(userPreferenceManager.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("Footer with Various Currencies: $£€ \u20A3\u20A4\u20A6\u20A7\u20A8\u20A9\u20AA\u20AB\u20AC\u20B1\u20B9\u20BA\u20BC\u20BD)");

        // Write the file
        writeFullReport(TripUtils.newDefaultTrip(), Collections.singletonList(factory.build()));

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(2, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createReportWithOtherNonWesternCharacters() throws Exception {

        // Configure test data
        final int count = 1;
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final ReceiptBuilderFactory factory = ReceiptUtils.newDefaultReceiptBuilderFactory(context);
        factory.setFile(imgFile);
        factory.setIsFullPage(true);
        factory.setName("Name with Non-Western Characters: \uCD9C \uFFE5 \u7172");
        when(userPreferenceManager.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("Footer with Various Currencies: $£€ \u20A3\u20A4\u20A6\u20A7\u20A8\u20A9\u20AA\u20AB\u20AC\u20B1\u20B9\u20BA\u20BC\u20BD)");

        // Write the file
        writeFullReport(TripUtils.newDefaultTripBuilderFactory().setDirectory(new File("Name with Non-Western Characters: \uCD9C \uFFE5 \u7172")).build(), Collections.singletonList(factory.build()));

        // Verify the results
        final int expectedNonWesternCharactersConvertedToImageCount = 3;
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(2, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count + expectedNonWesternCharactersConvertedToImageCount);
    }

    @Test
    public void createTableAndImageGridWithVarietyOfImagesToVerifyTableSplitting() throws Exception {

        // Configure test data
        final File normalReceiptImg = testResourceReader.openFile(TestResourceReader.RECEIPT_JPG);
        final File longReceiptImg = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final File wideReceiptImg = testResourceReader.openFile(TestResourceReader.WIDE_RECEIPT_JPG);

        final List<Receipt> receipts = new ArrayList<>();
        receipts.addAll(createReceiptsWithFile(normalReceiptImg, 1));
        receipts.addAll(createReceiptsWithFile(longReceiptImg, 2));
        receipts.addAll(createReceiptsWithFile(wideReceiptImg, 2));
        receipts.addAll(createReceiptsWithFile(wideReceiptImg, 1, true));
        receipts.addAll(createReceiptsWithFile(longReceiptImg, 3));
        receipts.addAll(createReceiptsWithFile(normalReceiptImg, 1, true));
        receipts.addAll(createReceiptsWithFile(longReceiptImg, 4));
        receipts.addAll(createReceiptsWithFile(longReceiptImg, 6));
        receipts.addAll(createReceiptsWithFile(wideReceiptImg, 2, true));
        receipts.addAll(createReceiptsWithFile(wideReceiptImg, 15));

        // Write the file
        writeFullReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(16, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, receipts.size());
    }

    @Test
    public void createImageGridWithVarietyOfImages() throws Exception {

        // Configure test data
        final File normalReceiptImg = testResourceReader.openFile(TestResourceReader.RECEIPT_JPG);
        final File longReceiptImg = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final File wideReceiptImg = testResourceReader.openFile(TestResourceReader.WIDE_RECEIPT_JPG);

        final List<Receipt> receipts = new ArrayList<>();
        receipts.addAll(createReceiptsWithFile(normalReceiptImg, 1));
        receipts.addAll(createReceiptsWithFile(longReceiptImg, 2));
        receipts.addAll(createReceiptsWithFile(wideReceiptImg, 2));
        receipts.addAll(createReceiptsWithFile(wideReceiptImg, 1, true));
        receipts.addAll(createReceiptsWithFile(longReceiptImg, 3));
        receipts.addAll(createReceiptsWithFile(normalReceiptImg, 1, true));
        receipts.addAll(createReceiptsWithFile(longReceiptImg, 4));
        receipts.addAll(createReceiptsWithFile(longReceiptImg, 6));
        receipts.addAll(createReceiptsWithFile(wideReceiptImg, 2, true));
        receipts.addAll(createReceiptsWithFile(wideReceiptImg, 3));

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(11, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, receipts.size());
    }

    @Test
    public void createImageGridWith1JpgReceiptThatIsNotFullPage() throws Exception {

        // Configure test data
        final int count = 1;
        final File imgFile = testResourceReader.openFile(TestResourceReader.RECEIPT_JPG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(1, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith2JpgReceiptsThatAreNotFullPage() throws Exception {

        // Configure test data
        final int count = 2;
        final File imgFile = testResourceReader.openFile(TestResourceReader.RECEIPT_JPG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(1, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith3JpgReceiptsThatAreNotFullPage() throws Exception {

        // Configure test data
        final int count = 3;
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(1, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith6JpgReceiptsThatAreNotFullPage() throws Exception {

        // Configure test data
        final int count = 6;
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(2, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith12JpgReceiptsThatAreNotFullPage() throws Exception {

        // Configure test data
        final int count = 12;
        final File imgFile = testResourceReader.openFile(TestResourceReader.RECEIPT_JPG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(3, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith2JpgReceiptsThatAreFullPage() throws Exception {

        // Configure test data
        final int count = 2;
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count, true);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(2, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith7JpgReceiptsThatAreFullPage() throws Exception {

        // Configure test data
        final int count = 7;
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count, true);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(7, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith1PngReceiptThatIsNotFullPage() throws Exception {

        // Configure test data
        final int count = 1;
        final File imgFile = testResourceReader.openFile(TestResourceReader.RECEIPT_PNG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(1, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith3PngReceiptsThatAreNotFullPage() throws Exception {

        // Configure test data
        final int count = 3;
        final File imgFile = testResourceReader.openFile(TestResourceReader.RECEIPT_PNG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(1, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith6PngReceiptsThatAreNotFullPage() throws Exception {

        // Configure test data
        final int count = 6;
        final File imgFile = testResourceReader.openFile(TestResourceReader.RECEIPT_PNG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(2, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createImageGridWith12PngReceiptsThatAreNotFullPage() throws Exception {

        // Configure test data
        final int count = 12;
        final File imgFile = testResourceReader.openFile(TestResourceReader.RECEIPT_PNG);
        final List<Receipt> receipts = createReceiptsWithFile(imgFile, count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts);

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(3, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @NonNull
    private List<Receipt> createReceiptsWithFile(@NonNull File file, int count) {
        return createReceiptsWithFile(file, count, false);
    }

    @NonNull
    private List<Receipt> createReceiptsWithFile(@NonNull File file, int count, boolean fullPage) {
        final List<Receipt> receipts = new ArrayList<>();
        final ReceiptBuilderFactory factory = ReceiptUtils.newDefaultReceiptBuilderFactory(context);
        factory.setFile(file);
        factory.setIsFullPage(fullPage);
        for (int i = 0; i < count; i++) {
            receipts.add(factory.build());
        }
        return receipts;
    }

    private void writeFullReport(@NonNull Trip trip, @NonNull List<Receipt> receipts) throws Exception {
        final PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(context, userPreferenceManager);

        final ArrayList<Column<Receipt>> receiptColumns = new ArrayList<>();
        receiptColumns.add(new ReceiptNameColumn(1, "Name", new DefaultSyncState()));
        receiptColumns.add(new ReceiptPriceColumn(2, "Price", new DefaultSyncState()));
        receiptColumns.add(new ReceiptDateColumn(3, "Date", new DefaultSyncState(), context, userPreferenceManager));
        receiptColumns.add(new ReceiptCategoryNameColumn(4, "Category name", new DefaultSyncState()));

        final List<Column<Distance>> distanceColumns = new ArrayList<>();
        distanceColumns.add(new DistanceLocationColumn(1, "Location", new DefaultSyncState(), context));
        distanceColumns.add(new DistancePriceColumn(2, "Price", new DefaultSyncState(), false));
        distanceColumns.add(new DistanceDistanceColumn(3, "Distance", new DefaultSyncState()));
        distanceColumns.add(new DistanceCurrencyColumn(4, "Currency", new DefaultSyncState()));
        distanceColumns.add(new DistanceRateColumn(5, "Rate", new DefaultSyncState()));
        distanceColumns.add(new DistanceDateColumn(6, "Date", new DefaultSyncState(), context, userPreferenceManager));
        distanceColumns.add(new DistanceCommentColumn(7, "Comment", new DefaultSyncState()));

        final List<Column<SumCategoryGroupingResult>> summationColumns = new ArrayList<>();
        summationColumns.add(new CategoryNameColumn(1, "Category", new DefaultSyncState()));
        summationColumns.add(new CategoryCodeColumn(2, "Category Code", new DefaultSyncState()));
        summationColumns.add(new CategoryPriceColumn(3, "Price", new DefaultSyncState()));
        summationColumns.add(new CategoryTaxColumn(4, "Tax", new DefaultSyncState()));
        summationColumns.add(new CategoryCurrencyColumn(5, "Currency", new DefaultSyncState()));

        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsTableSection(trip, receipts,
                receiptColumns, Collections.<Distance>emptyList(), distanceColumns,
                Collections.<SumCategoryGroupingResult>emptyList(), summationColumns,
                Collections.<CategoryGroupingResult>emptyList()));
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts));

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            pdfBoxReportFile.writeFile(outputStream, trip);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void writeImagesOnlyReport(@NonNull Trip trip, @NonNull List<Receipt> receipts) throws Exception {
        final PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(context, userPreferenceManager);
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts));

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            pdfBoxReportFile.writeFile(outputStream, trip);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /*
    @Test
    private void create() throws Exception {

        Trip trip = TripUtils.newDefaultTrip();

        List<Receipt> receipts = new ArrayList<>();
        ReceiptBuilderFactory factory = ReceiptUtils.newDefaultReceiptBuilderFactory(context);


        for (int i = 0; i < NUM_RECEIPTS; i++) {
            File file = null;
            if (i < NUM_IMAGES) {
                file = testResourceReader.openFile("pdf/" + String.valueOf(i + 1) + ".jpg");
            } else if (i < NUM_IMAGES + NUM_PDF) {
                file = testResourceReader.openFile("pdf/" + String.valueOf(i - NUM_IMAGES + 1) + ".pdf");
            }


            factory.setIsFullPage(i == 1);
            factory.setIsReimbursable(i % 2 == 0);
            Receipt receipt = createReceipt(
                    factory,
                    i + 1,
                    trip,
                    getReceiptTitle(i),
                    "Comment " + (i + 1),
                    file);

            receipts.add(receipt);
        }


        PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(context, userPreferenceManager);
        ArrayList<Column<Receipt>> columns = new ArrayList<>();
        columns.add(new ReceiptNameColumn(1, "Name", new DefaultSyncState()));
        columns.add(new ReceiptPriceColumn(2, "Price", new DefaultSyncState()));
        columns.add(new ReceiptDateColumn(3, "Date", new DefaultSyncState(), context, userPreferenceManager));
        columns.add(new ReceiptCategoryNameColumn(4, "Category name", new DefaultSyncState()));
        columns.add(new ReceiptIsReimbursableColumn(5, "Reimbursable", new DefaultSyncState(), context));
        columns.add(new ReceiptIsPicturedColumn(6, "Pictured", new DefaultSyncState(), context));

        List<Distance> distances = new ArrayList<>();
        DistanceBuilderFactory distanceFactory = new DistanceBuilderFactory();
        for (int i = 0; i < NUM_DISTANCES; i++) {
            distanceFactory.setTrip(trip);
            distanceFactory.setRate(20);
            distanceFactory.setDistance(10);
            distanceFactory.setCurrency(PriceCurrency.getInstance("USD"));
            distanceFactory.setLocation(i==0 ? "Loc 1" : "Location Location Location " + String.valueOf(i + 1));
            distances.add(distanceFactory.build());
        }

        final List<Column<Distance>> distanceColumns = new ArrayList<>();
        distanceColumns.add(new DistanceLocationColumn(1, "Location", new DefaultSyncState(), context));
        distanceColumns.add(new DistancePriceColumn(2, "Price", new DefaultSyncState(), false));
        distanceColumns.add(new DistanceDistanceColumn(3, "Distance", new DefaultSyncState()));
        distanceColumns.add(new DistanceCurrencyColumn(4, "Currency", new DefaultSyncState()));
        distanceColumns.add(new DistanceRateColumn(5, "Rate", new DefaultSyncState()));
        distanceColumns.add(new DistanceDateColumn(6, "Date", new DefaultSyncState(), context, userPreferenceManager));
        distanceColumns.add(new DistanceCommentColumn(7, "Comment", new DefaultSyncState()));


        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsTableSection(trip, receipts, columns, distances, distanceColumns));
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts));

        OutputStream os = new FileOutputStream(new File(OUTPUT_FILE));

        pdfBoxReportFile.writeFile(os, trip);

        os.close();
    }
    */

    private static void verifyImageCount(@NonNull PDDocument pdDocument, int expectedImageCount) throws Exception {
        int actualImageCount = 0;
        for (final PDPage page : pdDocument.getPages()) {
            final PDResources resources = page.getResources();
            for (COSName xObjectName : resources.getXObjectNames()) {
                final PDXObject xObject = resources.getXObject(xObjectName);
                if (xObject instanceof PDFormXObject) {
                    actualImageCount++;
                } else if (xObject instanceof PDImageXObject) {
                    actualImageCount++;
                }
            }
        }
        assertEquals("An incorrect amount of PDF images was rendered.", actualImageCount, expectedImageCount);
    }


}
