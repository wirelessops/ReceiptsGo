package com.wops.receiptsgo.workers.reports.pdf.pdfbox;

import androidx.annotation.NonNull;

import java.util.List;

import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import com.wops.receiptsgo.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;

public interface PdfBoxSectionFactory {

    @NonNull
    PdfBoxReceiptsTablePdfSection createReceiptsTableSection(
            @NonNull Trip trip,
            @NonNull List<Receipt> receipts,
            @NonNull List<Column<Receipt>> distances,
            @NonNull List<Distance> columns,
            @NonNull List<Column<Distance>> distanceColumns,
            @NonNull List<SumCategoryGroupingResult> categories,
            @NonNull List<Column<SumCategoryGroupingResult>> categoryColumns,
            @NonNull List<CategoryGroupingResult> groupingResults,
            @NonNull PurchaseWallet purchaseWallet);

    @NonNull
    PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection(@NonNull Trip trip,
                                                               @NonNull List<Receipt> receipts,
                                                               @NonNull List<Distance> distances);

}
