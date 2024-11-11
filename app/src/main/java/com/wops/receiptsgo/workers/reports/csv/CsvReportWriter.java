package com.wops.receiptsgo.workers.reports.csv;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * After having seen a few instances in which CSV files were failing for foreign languages when attempting
 * to open the file in MS Excel, I found this article that addresses some challenges and how we can
 * resolve them:
 * http://stackoverflow.com/questions/4192186/setting-a-utf-8-in-java-and-csv-file
 */
public class CsvReportWriter {

    private final File outputFile;

    public CsvReportWriter(@NonNull File outputFile) {
        this.outputFile = Preconditions.checkNotNull(outputFile);
    }

    public void write(@NonNull String csv) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.outputFile)) {
            IOUtils.write(ByteOrderMark.UTF_8.getBytes(), fileOutputStream);
            IOUtils.write(csv, fileOutputStream, "UTF-8");
        }
    }
}
