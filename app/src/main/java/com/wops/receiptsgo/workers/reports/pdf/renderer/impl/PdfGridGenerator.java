package com.wops.receiptsgo.workers.reports.pdf.renderer.impl;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.harmony.awt.AWTColor;

import java.util.ArrayList;
import java.util.List;

import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.filters.Filter;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.workers.reports.TableGenerator;
import com.wops.receiptsgo.workers.reports.pdf.colors.PdfColorStyle;
import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontSpec;
import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontStyle;
import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxContext;
import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxPageDecorations;
import com.wops.receiptsgo.workers.reports.pdf.renderer.Renderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.formatting.Padding;
import com.wops.receiptsgo.workers.reports.pdf.renderer.grid.GridRowRenderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.grid.PdfGridRenderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.imagex.PDImageXRenderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.imagex.PdfPDImageXFactory;
import com.wops.receiptsgo.workers.reports.pdf.renderer.imagex.PdfPDImageXFactoryFactory;


/**
 * Generates a table of images. Every image has a legend that is displayed above the image.
 */
public class PdfGridGenerator implements TableGenerator<List<Renderer>, Receipt> {

    private static final Padding DEFAULT_PADDING = new Padding(4f);
    private static final int FULL_PAGE_ROWS_COLS = 1;

    private final PdfBoxContext pdfBoxContext;
    private final PDDocument pdDocument;
    private final UserPreferenceManager userPreferenceManager;
    private final DateFormatter dateFormatter;
    private final PdfBoxPageDecorations decorations;
    private final Filter<Receipt> filter;
    private final AWTColor color;
    private final PdfFontSpec fontSpec;
    private final Padding padding;

    private final float availableWidth;
    private final float availableHeight;

    public PdfGridGenerator(@NonNull PdfBoxContext pdfBoxContext,
                            @NonNull PDDocument pdDocument,
                            @NonNull Filter<Receipt> filter,
                            @NonNull PdfBoxPageDecorations decorations,
                            float availableWidth,
                            float availableHeight) {
        this(pdfBoxContext, pdDocument, filter, decorations, pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default),
                pdfBoxContext.getFontManager().getFont(PdfFontStyle.Small), DEFAULT_PADDING, availableWidth, availableHeight);
    }

    public PdfGridGenerator(@NonNull PdfBoxContext pdfBoxContext,
                            @NonNull PDDocument pdDocument,
                            @NonNull Filter<Receipt> filter,
                            @NonNull PdfBoxPageDecorations decorations,
                            @NonNull AWTColor color,
                            @NonNull PdfFontSpec fontSpec,
                            @NonNull Padding padding,
                            float availableWidth,
                            float availableHeight) {
        this.pdfBoxContext = Preconditions.checkNotNull(pdfBoxContext);
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.filter = Preconditions.checkNotNull(filter);
        this.decorations = Preconditions.checkNotNull(decorations);
        this.userPreferenceManager = Preconditions.checkNotNull(pdfBoxContext.getPreferences());
        this.dateFormatter = Preconditions.checkNotNull(pdfBoxContext.getDateFormatter());
        this.color = Preconditions.checkNotNull(color);
        this.fontSpec = Preconditions.checkNotNull(fontSpec);
        this.padding = Preconditions.checkNotNull(padding);
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
    }


    @NonNull
    @Override
    public List<Renderer> generate(@NonNull List<Receipt> receipts) {

        final List<Renderer> renderers = new ArrayList<>();
        GridReceiptsRendererFactory rendererFactory = null;

        for (final Receipt receipt : receipts) {

            if (!filter.accept(receipt) || receipt.getFile() == null || !receipt.getFile().exists()) {
                Logger.info(this, "Filtering out {} from receipt table", receipt);
                continue;
            }

            if (receipt.getFile() == null || !receipt.getFile().exists()) {
                Logger.warn(this, "Missing file for {}. Omitting from receipt table", receipt);
                continue;
            }

            if (receipt.isFullPage() || receipt.hasPDF()) {
                if (rendererFactory != null && !rendererFactory.isEmpty()) {
                    Logger.debug(this, "Completing possible partial page as we have a full page entry");
                    constructRendererAndAddToList(rendererFactory, renderers);
                    rendererFactory = null;
                }

                if (receipt.hasPDF()) {
                    Logger.debug(this, "Adding existing pdf using the native renderer");
                    final PdfPDImageXFactory pdfFactory = new PdfPDImageXFactoryFactory(pdfBoxContext.getAndroidContext(), pdDocument, receipt.getFile()).get();
                    final ReceiptLabelTextRenderer textRenderer = new ReceiptLabelTextRenderer(receipt, pdfBoxContext.getAndroidContext(), pdDocument, userPreferenceManager, dateFormatter, color, fontSpec);
                    final PDImageXRenderer imageRenderer = new PDImageXRenderer(pdfFactory);
                    final PdfGridRenderer pdfGridRenderer = new PdfGridRenderer(pdfFactory, availableWidth, availableHeight);
                    pdfGridRenderer.addRow(new GridRowRenderer(textRenderer));
                    pdfGridRenderer.addRow(new GridRowRenderer(imageRenderer));
                    pdfGridRenderer.getRenderingFormatting().addFormatting(DEFAULT_PADDING);
                    renderers.add(pdfGridRenderer);
                } else {
                    Logger.debug(this, "Creating page for full page receipt.");
                    final GridReceiptsRendererFactory fullPageFactory = new GridReceiptsRendererFactory(pdfBoxContext.getAndroidContext(),
                            pdfBoxContext.getPreferences(), dateFormatter, pdDocument, decorations, FULL_PAGE_ROWS_COLS, FULL_PAGE_ROWS_COLS);
                    fullPageFactory.addReceipt(receipt);
                    constructRendererAndAddToList(fullPageFactory, renderers);
                }
            } else {
                if (rendererFactory == null) {
                    Logger.debug(this, "Creating new receipt grid for this pdf");
                    rendererFactory = new GridReceiptsRendererFactory(pdfBoxContext.getAndroidContext(),
                            pdfBoxContext.getPreferences(), dateFormatter, pdDocument, decorations);
                }

                rendererFactory.addReceipt(receipt);
                if (rendererFactory.isComplete()) {
                    Logger.debug(this, "NxN grid complete -- completing page");
                    constructRendererAndAddToList(rendererFactory, renderers);
                    rendererFactory = null;
                }
            }
        }

        // Add remaining cells (incomplete row)
        if (rendererFactory != null) {
            Logger.debug(this, "Writing final, incomplete page");
            constructRendererAndAddToList(rendererFactory, renderers);
        }

        return renderers;
    }

    private void constructRendererAndAddToList(@NonNull GridReceiptsRendererFactory rendererFactory, @NonNull List<Renderer> list) {
        Preconditions.checkNotNull(rendererFactory);
        Preconditions.checkNotNull(list);

        list.add(
            rendererFactory.buildSinglePageGrid(availableWidth, availableHeight, color, fontSpec, padding)
        );
    }

}
