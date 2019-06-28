package co.smartreceipts.android.imports.exceptions;

import androidx.annotation.NonNull;

public class InvalidPdfException extends Exception {

    public InvalidPdfException(@NonNull String message) {
        super(message);
    }
}
