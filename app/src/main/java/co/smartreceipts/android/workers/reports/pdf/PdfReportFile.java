package co.smartreceipts.android.workers.reports.pdf;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

public interface PdfReportFile {

    /**
     * Writes the pdf report into the stream that is passed.
     * The stream is not closed (the calling class, which is the one that provides the stream
     * should close it).
     * @param outStream
     * @param trip
     * @param receipts
     * @param distances
     * @return
     * @throws IOException
     */
    void writeFile(@NonNull OutputStream outStream, @NonNull Trip trip, @NonNull List<Receipt> receipts, @NonNull List<Distance> distances)
            throws IOException;



}