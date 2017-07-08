package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;

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
            @NonNull List<CategoryGroupingResult> groupingResults);

    @NonNull
    PdfBoxReceiptsImagesPdfSection createReceiptsImagesSection(@NonNull Trip trip,
                                                               @NonNull List<Receipt> receipts);

}
