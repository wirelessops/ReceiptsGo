package co.smartreceipts.android.workers.reports.pdf.renderer.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;
import co.smartreceipts.android.workers.reports.TableGenerator;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorStyle;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontStyle;
import co.smartreceipts.android.workers.reports.pdf.misc.ColumnWidthCalculator;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.WidthConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.BackgroundColor;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Padding;
import co.smartreceipts.android.workers.reports.pdf.renderer.grid.GridRowRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.text.TextRenderer;
import co.smartreceipts.android.workers.reports.pdf.utils.HeavyHandedReplaceIllegalCharacters;

public class PdfTableGenerator<DataType> implements TableGenerator<List<GridRowRenderer>, DataType> {

    private static final Padding DEFAULT_PADDING = new Padding(4f);

    private final PdfBoxContext pdfBoxContext;
    private final ReportResourcesManager reportResourcesManager;
    private final List<Column<DataType>> columns;
    private final PDDocument pdDocument;
    private final Filter<DataType> filter;
    private final boolean printHeaders;
    private final boolean printFooters;

    public PdfTableGenerator(@NonNull PdfBoxContext context,
                             @NonNull ReportResourcesManager reportResourcesManager,
                             @NonNull List<Column<DataType>> columns,
                             @NonNull PDDocument pdDocument,
                             @Nullable Filter<DataType> receiptFilter,
                             boolean printHeaders,
                             boolean printFooters) {
        this.pdfBoxContext = Preconditions.checkNotNull(context);
        this.reportResourcesManager = Preconditions.checkNotNull(reportResourcesManager);
        this.columns = Preconditions.checkNotNull(columns);
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.filter = receiptFilter;
        this.printHeaders = printHeaders;
        this.printFooters = printFooters;
    }

    @NonNull
    @Override
    public List<GridRowRenderer> generate(@NonNull List<DataType> list) throws IOException {
        final List<GridRowRenderer> gridRows = new ArrayList<>();
        final int colCount = columns.size();
        final List<DataType> filteredList = new ArrayList<>(list.size());

        float availableWidth = pdfBoxContext.getPageSize().getWidth() - 2 * pdfBoxContext.getPageMarginHorizontal();

        // calculate column widths
        // TODO: Include this as part of the measure pass
        float[] colWidths;
        ColumnWidthCalculator columnWidthCalculator = new ColumnWidthCalculator<>(pdfBoxContext,
                list, columns, availableWidth, DEFAULT_PADDING.value());
        colWidths = columnWidthCalculator.calculate();

        // Add the header
        final GridRowRenderer headerRow;
        if (printHeaders) {
            final List<TextRenderer> headerColumns = new ArrayList<>();
            for (int i = 0; i < colCount; i++) {
                final TextRenderer textRenderer = new TextRenderer(
                        pdfBoxContext.getAndroidContext(),
                        pdDocument,
                        reportResourcesManager.getFlexString(columns.get(i).getHeaderStringResId()),
                        pdfBoxContext.getColorManager().getColor(PdfColorStyle.Outline),
                        pdfBoxContext.getFontManager().getFont(PdfFontStyle.TableHeader));
                textRenderer.getRenderingFormatting().addFormatting(DEFAULT_PADDING);
                textRenderer.getRenderingConstraints().addConstraint(new WidthConstraint(colWidths[i]));
                headerColumns.add(textRenderer);
            }
            headerRow = new GridRowRenderer(headerColumns);
            headerRow.getRenderingFormatting().addFormatting(new BackgroundColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.TableHeader)));
            gridRows.add(headerRow);
        } else {
            headerRow = null;
        }

        if (!list.isEmpty()) {

            // Add each row
            for (int j = 0; j < list.size(); j++) {
                final DataType data = list.get(j);
                if (filter == null || filter.accept(data)) {
                    filteredList.add(data);
                    final List<TextRenderer> columnRenderers = new ArrayList<>();
                    for (int i = 0; i < colCount; i++) {
                        final TextRenderer textRenderer = new TextRenderer(
                                pdfBoxContext.getAndroidContext(),
                                pdDocument,
                                HeavyHandedReplaceIllegalCharacters.getSafeString(columns.get(i).getValue(data)),
                                pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default));
                        textRenderer.getRenderingFormatting().addFormatting(DEFAULT_PADDING);
                        textRenderer.getRenderingConstraints().addConstraint(new WidthConstraint(colWidths[i]));
                        columnRenderers.add(textRenderer);
                    }
                    final GridRowRenderer rowRenderer = new GridRowRenderer(columnRenderers);
                    if (j % 2 == 0) {
                        rowRenderer.getRenderingFormatting().addFormatting(new BackgroundColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.TableCell)));
                    }
                    rowRenderer.associateHeaderRow(headerRow);
                    gridRows.add(rowRenderer);
                }
            }
        }

        // Add the footer
        if (printFooters) {
            final List<TextRenderer> footerColumns = new ArrayList<>();
            for (int i = 0; i < colCount; i++) {
                final TextRenderer textRenderer = new TextRenderer(
                        pdfBoxContext.getAndroidContext(),
                        pdDocument,
                        columns.get(i).getFooter(filteredList),
                        pdfBoxContext.getColorManager().getColor(PdfColorStyle.Outline),
                        pdfBoxContext.getFontManager().getFont(PdfFontStyle.Default));
                textRenderer.getRenderingFormatting().addFormatting(DEFAULT_PADDING);
                textRenderer.getRenderingConstraints().addConstraint(new WidthConstraint(colWidths[i]));
                footerColumns.add(textRenderer);
            }
            final GridRowRenderer footerRow = new GridRowRenderer(footerColumns);
            footerRow.associateHeaderRow(headerRow);
            footerRow.getRenderingFormatting().addFormatting(new BackgroundColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.TableHeader)));
            gridRows.add(footerRow);
        }

        return gridRows;
    }

}
