package co.smartreceipts.android.apis;

import androidx.annotation.NonNull;

public class ApiValidationException extends Exception {

    public ApiValidationException(@NonNull String message) {
        super(message);
    }
}
