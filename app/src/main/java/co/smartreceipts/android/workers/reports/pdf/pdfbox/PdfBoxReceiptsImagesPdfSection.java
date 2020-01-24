package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.workers.reports.pdf.renderer.Renderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.impl.PdfGridGenerator;


public class PdfBoxReceiptsImagesPdfSection extends PdfBoxSection {

    private final PDDocument pdDocument;
    private final UserPreferenceManager userPreferenceManager;
    private final List<Receipt> receipts;
    private final List<Distance> distances;


    public PdfBoxReceiptsImagesPdfSection(@NonNull PdfBoxContext context, @NonNull PDDocument pdDocument,
                                          @NonNull Trip trip, @NonNull List<Receipt> receipts,
                                          @NonNull List<Distance> distances) {
        super(context, trip);
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.userPreferenceManager = Preconditions.checkNotNull(context.getPreferences());
        this.receipts = Preconditions.checkNotNull(receipts);
        this.distances = Preconditions.checkNotNull(distances);
    }

    @Override
    public void writeSection(@NonNull PDDocument doc, @NonNull PdfBoxWriter writer) throws IOException {

        DefaultPdfBoxPageDecorations pageDecorations = new DefaultPdfBoxPageDecorations(pdfBoxContext, trip, receipts, distances);

        float availableWidth = pdfBoxContext.getPageSize().getWidth() - 2 * pdfBoxContext.getPageMarginHorizontal();
        float availableHeight = pdfBoxContext.getPageSize().getHeight() - 2 * pdfBoxContext.getPageMarginVertical()
                - pageDecorations.getHeaderHeight() - pageDecorations.getFooterHeight();

        final PdfGridGenerator gridGenerator = new PdfGridGenerator(pdfBoxContext, pdDocument, new LegacyReceiptFilter(userPreferenceManager),
                pageDecorations, availableWidth, availableHeight);

        final List<Renderer> renderers = gridGenerator.generate(receipts);
        for (final Renderer renderer : renderers) {
            Logger.debug(this, "Performing measure of {} at {}.", renderer.getClass().getSimpleName(), System.currentTimeMillis());
            renderer.measure();
        }
        for (final Renderer renderer : renderers) {
            Logger.debug(this, "Performing render of {} at {}.", renderer.getClass().getSimpleName(), System.currentTimeMillis());
            renderer.render(writer);
        }
    }
}
