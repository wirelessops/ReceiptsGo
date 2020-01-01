package co.smartreceipts.android.workers.reports.pdf.renderer.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.harmony.awt.AWTColor;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.core.utils.log.Logger;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxPageDecorations;
import co.smartreceipts.android.workers.reports.pdf.renderer.Renderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.empty.EmptyRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Padding;
import co.smartreceipts.android.workers.reports.pdf.renderer.grid.GridRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.grid.GridRowRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.imagex.ImagePDImageXFactory;
import co.smartreceipts.android.workers.reports.pdf.renderer.imagex.PDImageXRenderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.pages.SinglePageRenderer;

public class GridReceiptsRendererFactory {

    private static final int DEFAULT_NUMBER_COLUMNS = 2;
    private static final int DEFAULT_NUMBER_ROWS = 2;

    private final Context context;
    private final UserPreferenceManager userPreferenceManager;
    private final DateFormatter dateFormatter;
    private final PDDocument pdDocument;
    private final PdfBoxPageDecorations decorations;
    private final List<Receipt> receipts = new ArrayList<>();
    private final int columns;
    private final int rows;

    public GridReceiptsRendererFactory(@NonNull Context context,
                                       @NonNull UserPreferenceManager userPreferenceManager,
                                       @NonNull DateFormatter dateFormatter,
                                       @NonNull PDDocument pdDocument,
                                       @NonNull PdfBoxPageDecorations decorations) {
        this(context, userPreferenceManager, dateFormatter, pdDocument, decorations, DEFAULT_NUMBER_COLUMNS, DEFAULT_NUMBER_ROWS);
    }

    public GridReceiptsRendererFactory(@NonNull Context context,
                                       @NonNull UserPreferenceManager userPreferenceManager,
                                       @NonNull DateFormatter dateFormatter,
                                       @NonNull PDDocument pdDocument,
                                       @NonNull PdfBoxPageDecorations decorations,
                                       int columns,
                                       int rows) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.decorations = Preconditions.checkNotNull(decorations);
        this.columns = columns;
        this.rows = rows;
    }

    public void addReceipt(@NonNull Receipt receipt) {
        receipts.add(Preconditions.checkNotNull(receipt));
    }

    public boolean isEmpty() {
        return receipts.isEmpty();
    }

    public boolean isComplete() {
        return receipts.size() == columns * rows;
    }

    @NonNull
    public SinglePageRenderer buildSinglePageGrid(float width, float height,
                                                  @NonNull AWTColor color, @NonNull PdfFontSpec fontSpec,
                                                  @NonNull Padding padding) {

        Logger.debug(this, "Building a {}x{} grid that contains {} receipts", rows, columns, receipts.size());
        final GridRenderer gridRenderer = new GridRenderer(width, height);
        for (int row = 0; row < rows; row++) {
            final List<Renderer> labelRows = new ArrayList<>();
            final List<Renderer> imageRows = new ArrayList<>();
            for (int column = 0; column < rows; column++) {
                final int index = (row * 2) + column;
                if (index < receipts.size()) {
                    final Receipt receipt = receipts.get(index);
                    Preconditions.checkNotNull(receipt.getFile(), "All receipts must have an image file");

                    labelRows.add(new ReceiptLabelTextRenderer(receipt, context, pdDocument, userPreferenceManager, dateFormatter, color, fontSpec));
                    if (receipt.hasImage()) {
                        imageRows.add(new PDImageXRenderer(new ImagePDImageXFactory(context, pdDocument, receipt.getFile())));
                    } else {
                        throw new IllegalArgumentException("Unsupported file type: " + receipt.getFile());
                    }
                } else {
                    labelRows.add(new EmptyRenderer());
                    imageRows.add(new EmptyRenderer(Renderer.MATCH_PARENT, Renderer.MATCH_PARENT));
                }
            }
            gridRenderer.addRow(new GridRowRenderer(labelRows));
            gridRenderer.addRow(new GridRowRenderer(imageRows));

            // Add spacing between rows so long as this isn't the last row
            if (row + 1 < rows) {
                final EmptyRenderer spacing = new EmptyRenderer(Renderer.MATCH_PARENT, decorations.getHeaderHeight() - 2 * padding.value());
                gridRenderer.addRow(new GridRowRenderer(spacing));
            }
        }
        gridRenderer.getRenderingFormatting().addFormatting(padding);

        return new SinglePageRenderer(gridRenderer);
    }


}
