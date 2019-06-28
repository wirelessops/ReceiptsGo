package co.smartreceipts.android.workers.reports;

import androidx.annotation.NonNull;

import java.io.File;

import co.smartreceipts.android.model.Trip;

/**
 * Defines a contract for how we can create a report. Users of this class should
 * use the {@link #generate(Trip)} method to build a report.
 */
public interface Report {

    /**
     * <p>
     * Builds a report that can be consumed by the end user
     * </p>
     * <p>
     * Must NOT be called on the UI thread.
     * </p>
     *
     * @param trip the parent {@link Trip} to generate
     * @return a file containing the report
     * @throws co.smartreceipts.android.workers.reports.ReportGenerationException if we failed to generate the report
     */
    @NonNull
    File generate(@NonNull Trip trip) throws ReportGenerationException;
}
