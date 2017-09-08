package co.smartreceipts.android.apis;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SmartReceiptsApiException extends Exception {

    private final Response response;
    private final SmartReceiptsApiErrorResponse errorResponse;

    public SmartReceiptsApiException(@NonNull Response response, @NonNull Exception exception, @NonNull Retrofit retrofit) {
        super(exception);

        // Map the response to our error type
        SmartReceiptsApiErrorResponse errorResponse = null;
        if (response.errorBody() != null) {
            try {
                final Converter<ResponseBody, SmartReceiptsApiErrorResponse> converter = retrofit.responseBodyConverter(SmartReceiptsApiErrorResponse.class, new Annotation[0]);
                errorResponse = converter.convert(response.errorBody());
            } catch (IOException e) {
                // Intentional no-op
            }
        }

        this.response = response;
        this.errorResponse = errorResponse;
    }

    @VisibleForTesting
    public SmartReceiptsApiException(@NonNull Response response, @NonNull Exception exception, @Nullable SmartReceiptsApiErrorResponse errorResponse) {
        super(exception);
        this.response = response;
        this.errorResponse = errorResponse;
    }

    @NonNull
    public Response getResponse() {
        return response;
    }

    @Nullable
    public SmartReceiptsApiErrorResponse getErrorResponse() {
        return errorResponse;
    }

    @Nullable
    public HttpUrl getUrl() {
        if (response != null) {
            return response.raw().request().url();
        } else {
            return null;
        }
    }
}
