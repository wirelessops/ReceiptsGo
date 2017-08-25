package co.smartreceipts.android.imports.importer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hadisatrio.optional.Optional;

import java.io.File;

import co.smartreceipts.android.ocr.apis.model.OcrResponse;

public class ActivityFileResultImporterResponse {

    private final Optional<Throwable> throwable;

    private final File file;
    private final OcrResponse ocrResponse;
    private final int requestCode;
    private final int resultCode;

    private ActivityFileResultImporterResponse(Optional<Throwable> throwable, @Nullable File file,
                                              @Nullable OcrResponse ocrResponse, int requestCode, int resultCode) {
        this.throwable = throwable;
        this.file = file;
        this.ocrResponse = ocrResponse;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
    }

    public static ActivityFileResultImporterResponse importerError(Throwable throwable) {
        return new ActivityFileResultImporterResponse(Optional.of(throwable), null, null, 0, 0);
    }

    public static ActivityFileResultImporterResponse importerResponse(@NonNull File file, @NonNull OcrResponse ocrResponse,
                                                                      int requestCode, int resultCode) {
        return new ActivityFileResultImporterResponse(Optional.absent(), file, ocrResponse, requestCode, resultCode);
    }

    /**
     * @return throwable if the importer produced an error
     */
    public Optional<Throwable> getThrowable() {
        return throwable;
    }

    /**
     * @return the resultant file that was imported
     */
    public File getFile() {
        return file;
    }

    /**
     * @return the OCR scan response
     */
    public OcrResponse getOcrResponse() {
        return ocrResponse;
    }

    /**
     * @return the request code that triggered the import
     */
    public int getRequestCode() {
        return requestCode;
    }

    /**
     * @return the result code that from the response
     */
    public int getResultCode() {
        return resultCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityFileResultImporterResponse that = (ActivityFileResultImporterResponse) o;

        if (requestCode != that.requestCode) return false;
        if (resultCode != that.resultCode) return false;
        if (!throwable.equals(that.throwable)) return false;
        if (file != null ? !file.equals(that.file) : that.file != null) return false;
        return ocrResponse != null ? ocrResponse.equals(that.ocrResponse) : that.ocrResponse == null;

    }

    @Override
    public int hashCode() {
        int result = throwable.hashCode();
        result = 31 * result + (file != null ? file.hashCode() : 0);
        result = 31 * result + (ocrResponse != null ? ocrResponse.hashCode() : 0);
        result = 31 * result + requestCode;
        result = 31 * result + resultCode;
        return result;
    }

    @Override
    public String toString() {
        return "ActivityFileResultImporterResponse{" +
                "throwable=" + throwable +
                ", file=" + file +
                ", ocrResponse=" + ocrResponse +
                ", requestCode=" + requestCode +
                ", resultCode=" + resultCode +
                '}';
    }
}
