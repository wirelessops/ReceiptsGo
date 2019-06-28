package co.smartreceipts.android.apis;

import androidx.annotation.Nullable;

import java.util.List;

public class SmartReceiptsApiErrorResponse {

    private List<String> errors;

    public SmartReceiptsApiErrorResponse(@Nullable List<String> errors) {
        this.errors = errors;
    }

    @Nullable
    public List<String> getErrors() {
        return errors;
    }
}
