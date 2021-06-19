package co.smartreceipts.android.report;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import co.smartreceipts.android.TestResourceReader;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.impl.columns.categories.CategoryCodeColumn;
import co.smartreceipts.android.model.impl.columns.categories.CategoryExchangedPriceColumn;
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
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.ReceiptUtils;
import co.smartreceipts.android.utils.TripUtils;
import co.smartreceipts.android.utils.shadows.ShadowFontFileFinder;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import co.smartreceipts.android.workers.reports.pdf.renderer.text.FallbackTextRenderer;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @Mock
    PurchaseWallet purchaseWallet;

    @Mock
    ReportResourcesManager reportResourcesManager;

    @Mock
    Trip mTrip;

    DateFormatter dateFormatter;

    /**
     * Base method, to be overridden by subclasses. The subclass must annotate the method
     * with the JUnit <code>@Before</code> annotation, and initialize the mocks.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        context = ApplicationProvider.getApplicationContext();
        testResourceReader = new TestResourceReader();
        dateFormatter = new DateFormatter(context, userPreferenceManager, Schedulers.trampoline());

        when(persistenceManager.getPreferenceManager()).thenReturn(userPreferenceManager);

        when(userPreferenceManager.get(UserPreference.General.DateSeparator)).thenReturn("/");
        when(userPreferenceManager.get(UserPreference.General.IncludeCostCenter)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.ReportOutput.PrintUserIdByPdfPhoto)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.ReportOutput.DefaultPdfPageSize)).thenReturn("A4");
        when(userPreferenceManager.get(UserPreference.ReportOutput.UserId)).thenReturn("");
        when(userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.Receipts.MinimumReceiptPrice)).thenReturn(-Float.MAX_VALUE);
        when(userPreferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Distance.PrintDistanceAsDailyReceiptInReports)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("Report generated using Smart Receipts for Android");
        when(userPreferenceManager.get(UserPreference.PlusSubscription.OmitDefaultTableInReports)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.PlusSubscription.CategoricalSummationInReports)).thenReturn(false);
        when(userPreferenceManager.get(UserPreference.PlusSubscription.SeparateByCategoryInReports)).thenReturn(false);

        when(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true);

        when(reportResourcesManager.getLocalizedContext()).thenReturn(context);
        when(reportResourcesManager.getFlexString(anyInt())).thenReturn("header");

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
        final DistanceBuilderFactory distanceFactory = new DistanceBuilderFactory(count);
        distanceFactory.setTrip(mTrip);
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final ReceiptBuilderFactory factory = ReceiptUtils.newDefaultReceiptBuilderFactory(context);
        factory.setFile(imgFile);
        factory.setIsFullPage(true);
        factory.setName("name\n\twith\r\nnon-printable\tchars");
        when(userPreferenceManager.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("footer\n\twith\r\nnon-printable\tchars");

        // Write the file
        writeFullReport(TripUtils.newDefaultTrip(), Collections.singletonList(factory.build()), Collections.singletonList(distanceFactory.build()));

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(2, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @Test
    public void createReportWithNonWesternCurrencies() throws Exception {

        // Configure test data
        final int count = 1;
        final DistanceBuilderFactory distanceFactory = new DistanceBuilderFactory(count);
        distanceFactory.setTrip(mTrip);
        final File imgFile = testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG);
        final ReceiptBuilderFactory factory = ReceiptUtils.newDefaultReceiptBuilderFactory(context);
        factory.setFile(imgFile);
        factory.setIsFullPage(true);
        factory.setName("Name with Various Currencies: $£€\u20A3\u20A4\u20A6\u20A7\u20A8\u20A9\u20AA\u20AB\u20AC\u20B1\u20B9\u20BA\u20BC\u20BD.");
        when(userPreferenceManager.get(UserPreference.PlusSubscription.PdfFooterString)).thenReturn("Footer with Various Currencies: $£€ \u20A3\u20A4\u20A6\u20A7\u20A8\u20A9\u20AA\u20AB\u20AC\u20B1\u20B9\u20BA\u20BC\u20BD)");

        // Write the file
        writeFullReport(TripUtils.newDefaultTrip(), Collections.singletonList(factory.build()), Collections.singletonList(distanceFactory.build()));

        // Verify the results
        final PDDocument pdDocument = PDDocument.load(outputFile);
        assertEquals(2, pdDocument.getNumberOfPages());
        verifyImageCount(pdDocument, count);
    }

    @LooperMode(LooperMode.Mode.LEGACY)
    @Test
    public void createReportWithOtherNonWesternCharacters() throws Exception {

        // Configure test data
        final int count = 1;

        final DistanceBuilderFactory distanceFactory = new DistanceBuilderFactory(count);
        distanceFactory.setTrip(mTrip);

        final ReceiptBuilderFactory receiptFactory = ReceiptUtils.newDefaultReceiptBuilderFactory(context);
        receiptFactory.setFile(testResourceReader.openFile(TestResourceReader.LONG_RECEIPT_JPG));
        receiptFactory.setIsFullPage(true);
        receiptFactory.setName("Name with Non-Western Characters: \uCD9C \uFFE5 \u7172");

        when(userPreferenceManager.get(UserPreference.PlusSubscription.PdfFooterString))
                .thenReturn("Footer with Various Currencies: $£€ \u20A3\u20A4\u20A6\u20A7\u20A8\u20A9\u20AA\u20AB\u20AC\u20B1\u20B9\u20BA\u20BC\u20BD)");

        // Write the file
        Trip trip = TripUtils.newDefaultTripBuilderFactory()
                .setDirectory(new File("Name with Non-Western Characters: \uCD9C \uFFE5 \u7172"))
                .build();
        List<Receipt> receipts = Collections.singletonList(receiptFactory.build());
        List<Distance> distances = Collections.singletonList(distanceFactory.build());

        writeFullReport(trip, receipts, distances);

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

        final List<Distance> distances = createDistances(37);

        // Write the file
        writeFullReport(TripUtils.newDefaultTrip(), receipts, distances);

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

        final List<Distance> distances = createDistances(25);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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
        final List<Distance> distances = createDistances(count);

        // Write the file
        writeImagesOnlyReport(TripUtils.newDefaultTrip(), receipts, distances);

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

    @NonNull
    private List<Distance> createDistances(int count) {
        final List<Distance> distances = new ArrayList<>();
        final DistanceBuilderFactory factory = new DistanceBuilderFactory(count);
        factory.setTrip(mTrip);
        for (int i = 0; i < count; i++) {
            distances.add(factory.build());
        }
        return distances;
    }

    private void writeFullReport(@NonNull Trip trip, @NonNull List<Receipt> receipts, @NonNull List<Distance> distances) throws Exception {
        final PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(reportResourcesManager, userPreferenceManager, dateFormatter);

        final ArrayList<Column<Receipt>> receiptColumns = new ArrayList<>();
        receiptColumns.add(new ReceiptNameColumn(1, new DefaultSyncState(), 0, UUID.randomUUID()));
        receiptColumns.add(new ReceiptPriceColumn(2, new DefaultSyncState(), 0, UUID.randomUUID()));
        receiptColumns.add(new ReceiptDateColumn(3, new DefaultSyncState(), dateFormatter, 0, UUID.randomUUID()));
        receiptColumns.add(new ReceiptCategoryNameColumn(4, new DefaultSyncState(), 0, UUID.randomUUID()));

        final List<Column<Distance>> distanceColumns = new ArrayList<>();
        distanceColumns.add(new DistanceLocationColumn(1, new DefaultSyncState(), context));
        distanceColumns.add(new DistancePriceColumn(2, new DefaultSyncState(), false));
        distanceColumns.add(new DistanceDistanceColumn(3, new DefaultSyncState()));
        distanceColumns.add(new DistanceCurrencyColumn(4, new DefaultSyncState()));
        distanceColumns.add(new DistanceRateColumn(5, new DefaultSyncState()));
        distanceColumns.add(new DistanceDateColumn(6, new DefaultSyncState(), dateFormatter));
        distanceColumns.add(new DistanceCommentColumn(7, new DefaultSyncState()));

        final List<Column<SumCategoryGroupingResult>> summationColumns = new ArrayList<>();
        summationColumns.add(new CategoryNameColumn(1, new DefaultSyncState()));
        summationColumns.add(new CategoryCodeColumn(2, new DefaultSyncState()));
        summationColumns.add(new CategoryPriceColumn(3, new DefaultSyncState()));
        summationColumns.add(new CategoryTaxColumn(4, new DefaultSyncState()));
        summationColumns.add(new CategoryExchangedPriceColumn(5, new DefaultSyncState()));

        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsTableSection(trip, receipts,
                receiptColumns, Collections.<Distance>emptyList(), distanceColumns,
                Collections.<SumCategoryGroupingResult>emptyList(), summationColumns,
                Collections.<CategoryGroupingResult>emptyList(), purchaseWallet));
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts, distances));

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            pdfBoxReportFile.writeFile(outputStream, trip, receipts, distances);
        }
    }

    private void writeImagesOnlyReport(@NonNull Trip trip, @NonNull List<Receipt> receipts, @NonNull List<Distance> distances) throws Exception {
        final PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(reportResourcesManager, userPreferenceManager, dateFormatter);
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts, distances));

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            pdfBoxReportFile.writeFile(outputStream, trip, receipts, distances);
        }
    }

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
