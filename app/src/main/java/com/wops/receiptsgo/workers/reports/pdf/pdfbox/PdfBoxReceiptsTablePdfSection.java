package com.wops.receiptsgo.workers.reports.pdf.pdfbox;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.filters.LegacyReceiptFilter;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.comparators.ReceiptDateComparator;
import com.wops.receiptsgo.model.converters.DistanceToReceiptsConverter;
import com.wops.receiptsgo.model.utils.ModelUtils;
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import com.wops.receiptsgo.purchases.model.InAppPurchase;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;
import com.wops.receiptsgo.workers.reports.pdf.colors.PdfColorStyle;
import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontStyle;
import com.wops.receiptsgo.workers.reports.pdf.renderer.empty.EmptyRenderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.formatting.Alignment;
import com.wops.receiptsgo.workers.reports.pdf.renderer.grid.GridRenderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.grid.GridRowRenderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.impl.PdfTableGenerator;
import com.wops.receiptsgo.workers.reports.pdf.renderer.text.TextRenderer;

public class PdfBoxReceiptsTablePdfSection extends PdfBoxSection {

    private static final int EMPTY_ROW_HEIGHT_NORMAL = 40;
    private static final int EMPTY_ROW_HEIGHT_SMALL = 10;

    private final List<Receipt> receipts;
    private final List<Column<Receipt>> receiptColumns;

    private final List<Distance> distances;
    private final List<Column<Distance>> distanceColumns;

    private final List<SumCategoryGroupingResult> categories;
    private final List<Column<SumCategoryGroupingResult>> categoryColumns;

    private final List<CategoryGroupingResult> groupingResults;

    private final UserPreferenceManager preferenceManager;
    private final PurchaseWallet purchaseWallet;

    private final ReportResourcesManager reportResourcesManager;

    private PdfBoxWriter writer;

    protected PdfBoxReceiptsTablePdfSection(@NonNull PdfBoxContext context,
                                            @NonNull ReportResourcesManager reportResourcesManager,
                                            @NonNull Trip trip,
                                            @NonNull List<Receipt> receipts,
                                            @NonNull List<Column<Receipt>> receiptColumns,
                                            @NonNull List<Distance> distances,
                                            @NonNull List<Column<Distance>> distanceColumns,
                                            @NonNull List<SumCategoryGroupingResult> categories,
                                            @NonNull List<Column<SumCategoryGroupingResult>> categoryColumns,
                                            @NonNull List<CategoryGroupingResult> groupingResults,
                                            @NonNull PurchaseWallet purchaseWallet) {
        super(context, trip);
        this.receipts = Preconditions.checkNotNull(receipts);
        this.distances = Preconditions.checkNotNull(distances);
        this.categories = Preconditions.checkNotNull(categories);
        this.groupingResults = Preconditions.checkNotNull(groupingResults);
        this.receiptColumns = Preconditions.checkNotNull(receiptColumns);
        this.distanceColumns = Preconditions.checkNotNull(distanceColumns);
        this.categoryColumns = Preconditions.checkNotNull(categoryColumns);
        this.preferenceManager = Preconditions.checkNotNull(context.getPreferences());
        this.purchaseWallet = Preconditions.checkNotNull(purchaseWallet);
        this.reportResourcesManager = Preconditions.checkNotNull(reportResourcesManager);
    }

    @Override
    public void writeSection(@NonNull PDDocument doc, @NonNull PdfBoxWriter writer) throws IOException {

        final DefaultPdfBoxPageDecorations pageDecorations = new DefaultPdfBoxPageDecorations(pdfBoxContext, trip, receipts, distances);
        final ReceiptsTotals totals = new ReceiptsTotals(trip, receipts, distances, preferenceManager);

        boolean hasPlusSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)
                || purchaseWallet.hasActivePurchase(InAppPurchase.PremiumSubscriptionPlan);

        // switch to landscape mode
        if (preferenceManager.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)) {
            pdfBoxContext.setPageSize(new PDRectangle(pdfBoxContext.getPageSize().getHeight(),
                    pdfBoxContext.getPageSize().getWidth()));
        }

        this.writer = writer;
        this.writer.newPage();

        final float availableWidth = pdfBoxContext.getPageSize().getWidth() - 2 * pdfBoxContext.getPageMarginHorizontal();
        final float availableHeight = pdfBoxContext.getPageSize().getHeight() - 2 * pdfBoxContext.getPageMarginVertical()
                - pageDecorations.getHeaderHeight() - pageDecorations.getFooterHeight();

        final GridRenderer gridRenderer = new GridRenderer(availableWidth, availableHeight);
        gridRenderer.addRows(writeHeader(trip, doc, totals));

        if (!receipts.isEmpty() &&
                (!hasPlusSubscription ||
                        (hasPlusSubscription && !preferenceManager.get(UserPreference.PlusSubscription.OmitDefaultTableInReports)))) {
            gridRenderer.addRow(new GridRowRenderer(new EmptyRenderer(0, EMPTY_ROW_HEIGHT_NORMAL)));
            gridRenderer.addRows(writeReceiptsTable(receipts, doc));
        }

        if (preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports) && !distances.isEmpty()) {
            gridRenderer.addRow(new GridRowRenderer(new EmptyRenderer(0, EMPTY_ROW_HEIGHT_NORMAL)));
            gridRenderer.addRows(writeDistancesTable(distances, doc));
        }

        if (hasPlusSubscription && preferenceManager.get(UserPreference.PlusSubscription.CategoricalSummationInReports)
                && !categories.isEmpty()) {
            gridRenderer.addRow(new GridRowRenderer(new EmptyRenderer(0, EMPTY_ROW_HEIGHT_NORMAL)));
            gridRenderer.addRows(writeCategoriesTable(categories, doc));
        }

        if (hasPlusSubscription && preferenceManager.get(UserPreference.PlusSubscription.SeparateByCategoryInReports)
                && !groupingResults.isEmpty()) {

            for (CategoryGroupingResult groupingResult : groupingResults) {
                gridRenderer.addRow(new GridRowRenderer(new EmptyRenderer(0, EMPTY_ROW_HEIGHT_NORMAL)));

                GridRowRenderer groupTitleRenderer = new GridRowRenderer(new TextRenderer(
                        pdfBoxContext.getAndroidContext(),
                        doc,
                        groupingResult.getCategory().getName(),
                        pdfBoxContext.getColorManager().getColor(PdfColorStyle.Outline),
                        pdfBoxContext.getFontManager().getFont(PdfFontStyle.TableHeader)));

                groupTitleRenderer.getRenderingFormatting().addFormatting(new Alignment(Alignment.Type.Start));

                GridRowRenderer paddingRenderer = new GridRowRenderer(new EmptyRenderer(0, EMPTY_ROW_HEIGHT_SMALL));

                gridRenderer.addRow(groupTitleRenderer);
                gridRenderer.addRow(paddingRenderer);
                gridRenderer.addRows(writeSeparateCategoryTable(groupingResult.getReceipts(), doc));
            }
        }

        gridRenderer.measure();
        gridRenderer.render(this.writer);

        // reset the page size if necessary
        if (preferenceManager.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)) {
            pdfBoxContext.setPageSize(new PDRectangle(pdfBoxContext.getPageSize().getHeight(),
                    pdfBoxContext.getPageSize().getWidth()));
        }
    }

    private List<GridRowRenderer> writeHeader(@NonNull Trip trip, @NonNull PDDocument pdDocument, @NonNull ReceiptsTotals data) throws IOException {

        // Print the report name as the title field
        final List<GridRowRenderer> headerRows = new ArrayList<>();
        headerRows.add(new GridRowRenderer(new TextRenderer(
                pdfBoxContext.getAndroidContext(),
                pdDocument,
                trip.getName(),
                pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Title))));

        // Print the From: StartDate To: EndDate
        final String fromToPeriod = pdfBoxContext.getString(R.string.report_header_duration,
                pdfBoxContext.getDateFormatter().getFormattedDate(trip.getStartDisplayableDate()),
                pdfBoxContext.getDateFormatter().getFormattedDate(trip.getEndDisplayableDate()));
        headerRows.add(new GridRowRenderer(new TextRenderer(
                pdfBoxContext.getAndroidContext(),
                pdDocument,
                fromToPeriod,
                pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));

        // Print the cost center (if present)
        if (preferenceManager.get(UserPreference.General.IncludeCostCenter) && !TextUtils.isEmpty(trip.getCostCenter())) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getString(R.string.report_header_cost_center, trip.getCostCenter()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }

        // Print the report comment (if present)
        if (!TextUtils.isEmpty(trip.getComment())) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getString(R.string.report_header_comment, trip.getComment()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }

        // Print the various tax totals if the IncludeTaxField is true and we have taxes
        if (preferenceManager.get(UserPreference.Receipts.IncludeTaxField) && !ModelUtils.isPriceZero(data.getTaxPrice())) {

            // Print receipts WITHOUT taxes
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getString(R.string.report_header_receipts_total_no_tax, data.getReceiptsWithOutTaxPrice().getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));

            // Print taxes
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getString(R.string.report_header_receipts_total_tax, data.getTaxPrice().getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));

            // Print receipts WITH taxes
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getString(R.string.report_header_receipts_total_with_tax, data.getReceiptsWithTaxPrice().getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        } else if (!distances.isEmpty()) {
            // Prints the receipts total if we have distances AND (the IncludeTaxField setting is false OR the value of taxes is 0)
            // We use this to distinguish receipts vs distances when we do NOT have the tax breakdown
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getString(R.string.report_header_receipts_total, data.getReceiptsWithTaxPrice().getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }

        // Print out the distances (if any)
        if (!distances.isEmpty()) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getString(R.string.report_header_distance_total, data.getDistancePrice().getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }

        // Print the grand total
        headerRows.add(new GridRowRenderer(new TextRenderer(
                pdfBoxContext.getAndroidContext(),
                pdDocument,
                pdfBoxContext.getString(R.string.report_header_grand_total, data.getGrandTotalPrice().getCurrencyFormattedPrice()),
                pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                pdfBoxContext.getFontManager().getFont(PdfFontStyle.DefaultBold))));

        // Print the grand total (reimbursable)
        if (!preferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)
                && !data.getGrandTotalPrice().equals(data.getReimbursableGrandTotalPrice())
                && data.getReimbursableGrandTotalPrice().getPrice().compareTo(BigDecimal.ZERO) != 0) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getString(R.string.report_header_receipts_total_reimbursable, data.getReimbursableGrandTotalPrice().getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.DefaultBold))));
        }

        for (final GridRowRenderer headerRow : headerRows) {
            headerRow.getRenderingFormatting().addFormatting(new Alignment(Alignment.Type.Start));
        }
        return headerRows;
    }

    private List<GridRowRenderer> writeReceiptsTable(@NonNull List<Receipt> receipts, @NonNull PDDocument pdDocument) throws IOException {

        final List<Receipt> receiptsTableList = new ArrayList<>(receipts);
        if (preferenceManager.get(UserPreference.Distance.PrintDistanceAsDailyReceiptInReports)) {
            receiptsTableList.addAll(new DistanceToReceiptsConverter(pdfBoxContext.getAndroidContext(), pdfBoxContext.getDateFormatter()).convert(distances));
            Collections.sort(receiptsTableList, new ReceiptDateComparator());
        }

        final PdfTableGenerator<Receipt> pdfTableGenerator = new PdfTableGenerator<>(pdfBoxContext,
                reportResourcesManager, receiptColumns, pdDocument, new LegacyReceiptFilter(preferenceManager),
                true, true);

        return pdfTableGenerator.generate(receiptsTableList);
    }

    private List<GridRowRenderer> writeDistancesTable(@NonNull List<Distance> distances, @NonNull PDDocument pdDocument) throws IOException {

        final PdfTableGenerator<Distance> pdfTableGenerator = new PdfTableGenerator<>(pdfBoxContext,
                reportResourcesManager, distanceColumns, pdDocument, null, true, true);
        return pdfTableGenerator.generate(distances);
    }

    private List<GridRowRenderer> writeCategoriesTable(@NonNull List<SumCategoryGroupingResult> categories, @NonNull PDDocument pdDocument) throws IOException {

        final PdfTableGenerator<SumCategoryGroupingResult> pdfTableGenerator = new PdfTableGenerator<>(pdfBoxContext,
                reportResourcesManager, categoryColumns, pdDocument, null, true, true);

        return pdfTableGenerator.generate(categories);
    }

    private List<GridRowRenderer> writeSeparateCategoryTable(@NonNull List<Receipt> receipts, @NonNull PDDocument pdDocument) throws IOException {

        final PdfTableGenerator<Receipt> pdfTableGenerator = new PdfTableGenerator<>(pdfBoxContext,
                reportResourcesManager, receiptColumns, pdDocument, null, true, true);

        return pdfTableGenerator.generate(receipts);
    }

}
