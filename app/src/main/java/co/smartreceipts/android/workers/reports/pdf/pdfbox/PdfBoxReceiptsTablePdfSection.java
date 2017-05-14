package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.comparators.ReceiptDateComparator;
import co.smartreceipts.android.model.converters.DistanceToReceiptsConverter;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorStyle;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontStyle;
import co.smartreceipts.android.workers.reports.pdf.renderer.empty.EmptyRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Alignment;
import co.smartreceipts.android.workers.reports.pdf.renderer.grid.GridRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.grid.GridRowRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.text.TextRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.impl.PdfTableGenerator;

public class PdfBoxReceiptsTablePdfSection extends PdfBoxSection {

    private static final float EPSILON = 0.0001f;
    
    private final List<Receipt> mReceipts;
    private final List<Column<Receipt>> mReceiptColumns;

    private final List<Distance> mDistances;
    private final List<Column<Distance>> mDistanceColumns;
    private final UserPreferenceManager mPreferences;

    private PdfBoxWriter mWriter;

    protected PdfBoxReceiptsTablePdfSection(@NonNull PdfBoxContext context,
                                            @NonNull Trip trip,
                                            @NonNull List<Receipt> receipts,
                                            @NonNull List<Column<Receipt>> receiptColumns,
                                            @NonNull List<Distance> distances,
                                            @NonNull List<Column<Distance>> distanceColumns) {
        super(context, trip);
        mReceipts = Preconditions.checkNotNull(receipts);
        mDistances = Preconditions.checkNotNull(distances);
        mReceiptColumns = Preconditions.checkNotNull(receiptColumns);
        mPreferences = Preconditions.checkNotNull(context.getPreferences());
        mDistanceColumns = Preconditions.checkNotNull(distanceColumns);
    }



    @Override
    public void writeSection(@NonNull PDDocument doc, @NonNull PdfBoxWriter writer) throws IOException {

        final DefaultPdfBoxPageDecorations pageDecorations = new DefaultPdfBoxPageDecorations(pdfBoxContext, trip);
        final ReceiptsTotals totals = new ReceiptsTotals(trip, mReceipts, mDistances, mPreferences);

        // switch to landscape mode
        if (mPreferences.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)) {
            pdfBoxContext.setPageSize(new PDRectangle(pdfBoxContext.getPageSize().getHeight(),
                    pdfBoxContext.getPageSize().getWidth()));
        }

        mWriter = writer;
        mWriter.newPage();

        final float availableWidth = pdfBoxContext.getPageSize().getWidth() - 2 * pdfBoxContext.getPageMarginHorizontal();
        final float availableHeight = pdfBoxContext.getPageSize().getHeight() - 2 * pdfBoxContext.getPageMarginVertical()
                - pageDecorations.getHeaderHeight() - pageDecorations.getFooterHeight();

        final GridRenderer gridRenderer = new GridRenderer(availableWidth, availableHeight);
        gridRenderer.addRows(writeHeader(trip, doc, totals));
        gridRenderer.addRow(new GridRowRenderer(new EmptyRenderer(0, 40)));
        gridRenderer.addRows(writeReceiptsTable(mReceipts, doc));

        if (mPreferences.get(UserPreference.Distance.PrintDistanceTableInReports) && !mDistances.isEmpty()) {
            gridRenderer.addRow(new GridRowRenderer(new EmptyRenderer(0, 40)));
            gridRenderer.addRows(writeDistancesTable(mDistances, doc));
        }

        gridRenderer.measure();
        gridRenderer.render(mWriter);

        // reset the page size if necessary
        if (mPreferences.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)) {
            pdfBoxContext.setPageSize(new PDRectangle(pdfBoxContext.getPageSize().getHeight(),
                    pdfBoxContext.getPageSize().getWidth()));
        }
    }

    private List<GridRowRenderer> writeHeader(@NonNull Trip trip, @NonNull PDDocument pdDocument, @NonNull ReceiptsTotals data) throws IOException {

        final List<GridRowRenderer> headerRows = new ArrayList<>();
        headerRows.add(new GridRowRenderer(new TextRenderer(
                pdfBoxContext.getAndroidContext(),
                pdDocument,
                trip.getName(),
                pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Title))));
        
        if (!data.receiptsPrice.equals(data.netPrice)) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getAndroidContext().getString(R.string.report_header_receipts_total, data.receiptsPrice.getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }

        if (mPreferences.get(UserPreference.Receipts.IncludeTaxField)) {
            if (mPreferences.get(UserPreference.Receipts.UsePreTaxPrice) && data.taxPrice.getPriceAsFloat() > EPSILON) {
                headerRows.add(new GridRowRenderer(new TextRenderer(
                        pdfBoxContext.getAndroidContext(),
                        pdDocument,
                        pdfBoxContext.getAndroidContext().getString(R.string.report_header_receipts_total_tax, data.taxPrice.getCurrencyFormattedPrice()),
                        pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                        pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
            } else if (!data.noTaxPrice.equals(data.receiptsPrice) && data.noTaxPrice.getPriceAsFloat() > EPSILON) {
                headerRows.add(new GridRowRenderer(new TextRenderer(
                        pdfBoxContext.getAndroidContext(),
                        pdDocument,
                        pdfBoxContext.getAndroidContext().getString(R.string.report_header_receipts_total_no_tax, data.noTaxPrice.getCurrencyFormattedPrice()),
                        pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                        pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
            }
        }

        if (!mPreferences.get(UserPreference.Receipts.OnlyIncludeReimbursable) && !data.reimbursablePrice.equals(data.receiptsPrice)) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getAndroidContext().getString(R.string.report_header_receipts_total_reimbursable, data.reimbursablePrice.getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }
        if (!mDistances.isEmpty()) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getAndroidContext().getString(R.string.report_header_distance_total, data.distancePrice.getCurrencyFormattedPrice()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }

        headerRows.add(new GridRowRenderer(new TextRenderer(
                pdfBoxContext.getAndroidContext(),
                pdDocument,
                pdfBoxContext.getAndroidContext().getString(R.string.report_header_gross_total, data.netPrice.getCurrencyFormattedPrice()),
                pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));

        String fromToPeriod = pdfBoxContext.getString(R.string.report_header_from,
                trip.getFormattedStartDate(pdfBoxContext.getAndroidContext(), mPreferences.get(UserPreference.General.DateSeparator)))
                + " "
                + pdfBoxContext.getString(R.string.report_header_to,
                trip.getFormattedEndDate(pdfBoxContext.getAndroidContext(), mPreferences.get(UserPreference.General.DateSeparator)));

        headerRows.add(new GridRowRenderer(new TextRenderer(
                pdfBoxContext.getAndroidContext(),
                pdDocument,
                fromToPeriod,
                pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));


        if (mPreferences.get(UserPreference.General.IncludeCostCenter) && !TextUtils.isEmpty(trip.getCostCenter())) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getAndroidContext().getString(R.string.report_header_cost_center, trip.getCostCenter()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }
        if (!TextUtils.isEmpty(trip.getComment())) {
            headerRows.add(new GridRowRenderer(new TextRenderer(
                    pdfBoxContext.getAndroidContext(),
                    pdDocument,
                    pdfBoxContext.getAndroidContext().getString(R.string.report_header_comment, trip.getComment()),
                    pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                    pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default))));
        }

        for (final GridRowRenderer headerRow : headerRows) {
            headerRow.getRenderingFormatting().addFormatting(new Alignment(Alignment.Type.Left));
        }
        return headerRows;
    }

    private List<GridRowRenderer>  writeReceiptsTable(@NonNull List<Receipt> receipts, @NonNull PDDocument pdDocument) throws IOException {

        final List<Receipt> receiptsTableList = new ArrayList<>(receipts);
        if (mPreferences.get(UserPreference.Distance.PrintDistanceAsDailyReceiptInReports)) {
            receiptsTableList.addAll(new DistanceToReceiptsConverter(pdfBoxContext.getAndroidContext(), mPreferences).convert(mDistances));
            Collections.sort(receiptsTableList, new ReceiptDateComparator());
        }

        final PdfTableGenerator<Receipt> pdfTableGenerator = new PdfTableGenerator<>(pdfBoxContext, mReceiptColumns,
                pdDocument, new LegacyReceiptFilter(mPreferences), true, true);

        return pdfTableGenerator.generate(receiptsTableList);
    }

    private List<GridRowRenderer> writeDistancesTable(@NonNull List<Distance> distances, @NonNull PDDocument pdDocument) throws IOException {
        final PdfTableGenerator<Distance> pdfTableGenerator = new PdfTableGenerator<>(pdfBoxContext, mDistanceColumns,
                pdDocument, null, true, true);
        return pdfTableGenerator.generate(distances);
    }


}
