package com.wops.receiptsgo.apis;

import androidx.annotation.NonNull;

public class ApiValidationException extends Exception {

    public ApiValidationException(@NonNull String message) {
        super(message);
    }
}
