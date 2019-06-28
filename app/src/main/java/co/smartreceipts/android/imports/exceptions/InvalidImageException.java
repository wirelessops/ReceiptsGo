package co.smartreceipts.android.imports.exceptions;

import androidx.annotation.NonNull;

public class InvalidImageException extends Exception {

    public InvalidImageException(@NonNull String message) {
        super(message);
    }
}
