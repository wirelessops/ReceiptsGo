package co.smartreceipts.android.imports.exceptions;

import android.support.annotation.NonNull;

public class InvalidImageException extends Exception {

    public InvalidImageException(@NonNull String message) {
        super(message);
    }
}
