package com.wops.receiptsgo.workers.reports.pdf.pdfbox;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;
import com.wops.receiptsgo.workers.reports.pdf.PdfReportFile;
import com.wops.receiptsgo.workers.reports.pdf.colors.PdfColorManager;
import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontManager;

public class PdfBoxReportFile implements PdfReportFile, PdfBoxSectionFactory {

    private final DefaultPdfBoxContext pdfBoxContext;
    private final ReportResourcesManager reportResourcesManager;
    private final PDDocument pdDocument;
    private final List<PdfBoxSection> sections;

    public PdfBoxReportFile(@NonNull ReportResourcesManager reportResourcesManager,
                            @NonNull UserPreferenceManager preferences,
                            @NonNull DateFormatter dateFormatter) throws IOException {

        this.reportResourcesManager = Preconditions.checkNotNull(reportResourcesManager);

        pdDocument = new PDDocument();
        sections = new ArrayList<>();

        final PdfColorManager colorManager = new PdfColorManager();
        final PdfFontManager fontManager = new PdfFontManager(reportResourcesManager.getLocalizedContext(), pdDocument);
        fontManager.initialize();

        pdfBoxContext = new DefaultPdfBoxContext(reportResourcesManager.getLocalizedContext(),
                fontManager, colorManager, preferences, dateFormatter);
    }

    @Override
    public void writeFile(@NonNull OutputStream outStream, @NonNull Trip trip, @NonNull List<Receipt> receipts,
                          @NonNull List<Distance> distances) throws IOException {
        try {
            final PdfBoxWriter writer = new PdfBoxWriter(pdDocument, pdfBoxContext, new DefaultPdfBoxPageDecorations(pdfBoxContext, trip, receipts, distances));
            for (PdfBoxSection section : sections) {
                section.writeSection(pdDocument, writer);
            }
            writer.writeAndClose();

            pdDocument.save(outStream);
        } finally {
            try {
                pdDocument.close();
            } catch (IOException e) {
                Logger.error(this, e);
            }
        }
    }

    public void addSection(PdfBoxSection section) {
        sections.add(section);
    }

    @NonNull
    @Override
    public PdfBoxReceiptsTablePdfSection createReceiptsTableSection(@NonNull Trip trip,
                                                                    @NonNull List<Receipt> receipts,
                                                                    @NonNull List<Column<Receipt>> columns,
                                                                    @NonNull List<Distance> distances,
                                                                    @NonNull List<Column<Distance>> distanceColumns,
                                                                    @NonNull List<SumCategoryGroupingResult> categories,
                                                                    @NonNull List<Column<SumCategoryGroupingResult>> categoryColumns,
                                                                    @NonNull List<CategoryGroupingResult> groupingResults,
                                                                    @NonNull PurchaseWallet purchaseWallet) {

        return new PdfBoxReceiptsTablePdfSection(pdfBoxContext, reportResourcesManager, trip,
                receipts, columns, distances, distanceColumns, categories, categoryColumns,
                groupingResults, purchaseWallet);
    }

    @NonNull
    @Override
    public PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection(@NonNull Trip trip,
                                                                      @NonNull List<Receipt> receipts,
                                                                      @NonNull List<Distance> distances) {
        return new PdfBoxReceiptsImagesPdfSection(pdfBoxContext, pdDocument, trip, receipts, distances);
    }
}