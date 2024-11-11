package com.wops.receiptsgo.workers.reports.pdf;

import androidx.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxWriter;

public interface PdfSection {
    void writeSection(@NonNull PDDocument doc, @NonNull PdfBoxWriter writer) throws IOException;
}
