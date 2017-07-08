package co.smartreceipts.android.workers.reports.pdf;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.impl.columns.categories.CategoryColumnDefinitions;
import co.smartreceipts.android.model.impl.columns.distance.DistanceColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.grouping.GroupingController;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxReportFile;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

public class PdfBoxFullPdfReport extends PdfBoxAbstractReport {

    private final GroupingController groupingController;
    private final PurchaseWallet purchaseWallet;

    public PdfBoxFullPdfReport(Context context, DatabaseHelper db,
                               UserPreferenceManager preferences,
                               StorageManager storageManager, Flex flex, PurchaseWallet purchaseWallet) {
        super(context, db, preferences, storageManager, flex);
        this.groupingController = new GroupingController(db, context, preferences);
        this.purchaseWallet = purchaseWallet;

    }

    @Override
    public void createSections(@NonNull Trip trip, PdfBoxReportFile pdfBoxReportFile) {
        // Receipts Table
        final List<Receipt> receipts = new ArrayList<>(getDatabase().getReceiptsTable().getBlocking(trip, false));
        final List<Column<Receipt>> columns = getDatabase().getPDFTable().get().blockingGet();

        // Distance Table
        final ColumnDefinitions<Distance> distanceColumnDefinitions = new DistanceColumnDefinitions(getContext(), getDatabase(), getPreferences(), getFlex(), true);
        final List<Distance> distances = new ArrayList<>(getDatabase().getDistanceTable().getBlocking(trip, false));
        final List<Column<Distance>> distanceColumns = distanceColumnDefinitions.getAllColumns();

        // Categories Summation Table
        final List<Column<SumCategoryGroupingResult>> categoryColumns = new CategoryColumnDefinitions(getContext())
                .getAllColumns();
        final List<SumCategoryGroupingResult> categories = groupingController.getSummationByCategory(trip).toList().blockingGet();

        // Grouping by Category Receipts Tables
        final List<CategoryGroupingResult> groupingResults = groupingController.getReceiptsGroupedByCategory(trip).toList().blockingGet();


        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsTableSection(trip,
                receipts, columns, distances, distanceColumns, categories, categoryColumns,
                groupingResults, purchaseWallet));
        pdfBoxReportFile.addSection(pdfBoxReportFile.createReceiptsImagesSection(trip, receipts));
    }

}
