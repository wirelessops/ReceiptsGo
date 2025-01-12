package com.wops.receiptsgo.workers.reports.pdf;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.workers.reports.AbstractReport;
import com.wops.receiptsgo.workers.reports.ReportGenerationException;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;
import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import wb.android.storage.StorageManager;

public abstract class PdfBoxAbstractReport extends AbstractReport {

    public PdfBoxAbstractReport(@NonNull ReportResourcesManager reportResourcesManager,
                                @NonNull DatabaseHelper db,
                                @NonNull UserPreferenceManager preferences,
                                @NonNull StorageManager storageManager,
                                @NonNull DateFormatter dateFormatter) {
        super(reportResourcesManager, db, preferences, storageManager, dateFormatter);
    }

    @NonNull
    @Override
    public File generate(@NonNull Trip trip) throws ReportGenerationException {
        final String outputFileName = getFileName(trip);
        FileOutputStream pdfStream = null;

        try {
            getStorageManager().delete(trip.getDirectory(), outputFileName);

            pdfStream = getStorageManager().getFOS(trip.getDirectory(), outputFileName);

            PdfBoxReportFile pdfBoxReportFile = new PdfBoxReportFile(getReportResourcesManager(), getPreferences(), getDateFormatter());

            createSections(trip, pdfBoxReportFile);

            final List<Receipt> receipts = new ArrayList<>(getDatabase().getReceiptsTable().getBlocking(trip, false));
            final List<Distance> distances = new ArrayList<>(getDatabase().getDistanceTable().getBlocking(trip, false));

            pdfBoxReportFile.writeFile(pdfStream, trip, receipts, distances);

            return getStorageManager().getFile(trip.getDirectory(), outputFileName);

        } catch (IOException e) {
            Logger.error(this, e);
            throw new ReportGenerationException(e);
        } finally {
            if (pdfStream != null) {
                StorageManager.closeQuietly(pdfStream);
            }
        }

    }

    public abstract void createSections(@NonNull Trip trip, PdfBoxReportFile pdfBoxReportFile);

    protected String getFileName(Trip trip) {
        return trip.getDirectory().getName() + ".pdf";
    }
}
