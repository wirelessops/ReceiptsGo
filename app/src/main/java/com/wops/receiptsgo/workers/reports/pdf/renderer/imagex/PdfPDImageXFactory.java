package com.wops.receiptsgo.workers.reports.pdf.renderer.imagex;

import java.io.Closeable;
import java.io.IOException;

public interface PdfPDImageXFactory extends PDImageXFactory, Closeable {

    /**
     * Opens the PDF file, allowing us to consume the PDF content
     *
     * @throws IOException if any disk operations fail
     * @throws SecurityException if the PDF is password protected
     */
    void open() throws IOException, SecurityException;

    /**
     * Bumps us to use the next page
     *
     * @return {@code false} if we're on the last page. {@code true} otherwise
     */
    boolean nextPage();

}
