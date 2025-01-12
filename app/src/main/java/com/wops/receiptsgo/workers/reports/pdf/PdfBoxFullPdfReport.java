package com.wops.receiptsgo.workers.reports.pdf;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.impl.columns.categories.CategoryColumnDefinitions;
import com.wops.receiptsgo.model.impl.columns.distance.DistanceColumnDefinitions;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.persistence.database.controllers.grouping.GroupingController;
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.workers.reports.ReportResourcesManager;
import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import wb.android.storage.StorageManager;

public class PdfBoxFullPdfReport extends PdfBoxAbstractReport {

    private final GroupingController groupingController;
    private final PurchaseWallet purchaseWallet;

    public PdfBoxFullPdfReport(ReportResourcesManager reportResourcesManager,
                               DatabaseHelper db,
                               UserPreferenceManager preferences,
                               StorageManager storageManager,
                               PurchaseWallet purchaseWallet,
                               DateFormatter dateFormatter) {
        super(reportResourcesManager, db, preferences, storageManager, dateFormatter);
        this.groupingController = new GroupingController(db, reportResourcesManager.getLocalizedContext(), preferences);
        this.purchaseWallet = purchaseWallet;
    }

    @Override
    public void createSections(@NonNull Trip trip, PdfBoxReportFile pdfBoxReportFile) {
        // Receipts Table
        final List<Receipt> receipts = new ArrayList<>(getDatabase().getReceiptsTable().getBlocking(trip, false));
        final List<Column<Receipt>> columns = getDatabase().getPDFTable().get().blockingGet();

        // Distance Table
        final ColumnDefinitions<Distance> distanceColumnDefinitions = new DistanceColumnDefinitions(getReportResourcesManager(), getPreferences(), getDateFormatter(), true);
        final List<Distance> distances = new ArrayList<>(getDatabase().getDistanceTable().getBlocking(trip, false));
        final List<Column<Distance>> distanceColumns = distanceColumnDefinitions.getAllColumns();

        // Categories Summation Table
        final List<SumCategoryGroupingResult> categories = groupingController.getSummationByCategory(trip).toList().blockingGet();

        boolean isMultiCurrency = false;
        for (SumCategoryGroupingResult categorySummation : categories) {
            if (categorySummation.isMultiCurrency()) {
                isMultiCurrency = true;
                break;
            }
        }

        boolean taxEnabled = userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField);

        final List<Column<SumCategoryGroupingResult>> categoryColumns = new CategoryColumnDefinitions(getReportResourcesManager(), isMultiCurrency, taxEnabled)
                .getAllColumns();

        // Grouping by Category Receipts Tables
        final List<CategoryGroupingResult> groupingResults = groupingController.getReceiptsGroupedByCategory(trip).toList().blockingGet();

        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsTableSection(trip,
                receipts, columns, distances, distanceColumns, categories, categoryColumns,
                groupingResults, purchaseWallet));
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts, distances));
    }

}
