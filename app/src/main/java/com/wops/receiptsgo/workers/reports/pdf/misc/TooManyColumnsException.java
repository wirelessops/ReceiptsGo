package com.wops.receiptsgo.workers.reports.pdf.misc;

import androidx.annotation.NonNull;

import java.io.IOException;

public class TooManyColumnsException extends IOException {

    public TooManyColumnsException() {
        super("Cannot squeeze all columns in a single page");
    }

    public TooManyColumnsException(@NonNull Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
