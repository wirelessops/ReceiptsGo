package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.workers.reports.pdf.PdfSection;

public abstract class PdfBoxSection implements PdfSection {

    protected final PdfBoxContext pdfBoxContext;
    protected final Trip trip;


    public PdfBoxSection(@NonNull PdfBoxContext context, @NonNull Trip trip) {
        this.pdfBoxContext = Preconditions.checkNotNull(context);
        this.trip = Preconditions.checkNotNull(trip);
    }
}
