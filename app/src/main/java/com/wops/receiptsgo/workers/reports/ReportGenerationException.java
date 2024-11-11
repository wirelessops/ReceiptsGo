package com.wops.receiptsgo.workers.reports;

import androidx.annotation.NonNull;

public class ReportGenerationException extends Exception {

    private final boolean mIsFatal;

    public ReportGenerationException(@NonNull Throwable cause) {
        this(cause.getMessage(), cause, true);
    }

    public ReportGenerationException(@NonNull String message, boolean isFatal) {
        this(message, null, isFatal);
    }

    public ReportGenerationException(@NonNull String message, Throwable cause, boolean isFatal) {
        super(message, cause);
        mIsFatal = isFatal;
    }

    public final boolean isFatal() {
        return mIsFatal;
    }
}
