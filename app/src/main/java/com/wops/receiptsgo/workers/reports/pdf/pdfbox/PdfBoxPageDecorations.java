package com.wops.receiptsgo.workers.reports.pdf.pdfbox;


import androidx.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

/**
 * Responsible for printing out (static at the moment) headers and footers to the
 * pdf report pages.
 */
public interface PdfBoxPageDecorations {

    /**
     * Writes the page header in the <code>contentStream</code> passed
     *
     * @param contentStream
     * @throws IOException
     */
    void writeHeader(@NonNull PDPageContentStream contentStream) throws IOException;

    /**
     * Writes the page footer in the <code>contentStream</code> passed
     *
     * @param contentStream
     * @throws IOException
     */
    void writeFooter(@NonNull PDPageContentStream contentStream) throws IOException;

    /**
     * Returns the amount of space that should be reserved for the header
     *
     * @return
     */
    float getHeaderHeight();

    /**
     * Returns the amount of space that should be reserved for the footer
     *
     * @return
     */
    float getFooterHeight();
}
