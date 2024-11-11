package com.wops.receiptsgo.workers.reports.pdf;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;
import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import wb.android.storage.StorageManager;

public class PdfBoxImagesOnlyReport extends PdfBoxAbstractReport {

    public PdfBoxImagesOnlyReport(@NonNull ReportResourcesManager reportResourcesManager,
                                  @NonNull DatabaseHelper db,
                                  @NonNull UserPreferenceManager preferences,
                                  @NonNull StorageManager storageManager,
                                  DateFormatter dateFormatter) {
        super(reportResourcesManager, db, preferences, storageManager, dateFormatter);
    }

    @Override
    public void createSections(@NonNull Trip trip, @NonNull PdfBoxReportFile pdfBoxReportFile) {
        final List<Receipt> receipts = new ArrayList<>(getDatabase().getReceiptsTable().getBlocking(trip, false));
        final List<Distance> distances = new ArrayList<>(getDatabase().getDistanceTable().getBlocking(trip, false));
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts, distances));
    }

    @Override
    protected String getFileName(Trip trip) {
        return trip.getDirectory().getName() + "Images.pdf";
    }
}
