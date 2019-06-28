package co.smartreceipts.android.ocr.apis.model;

import androidx.annotation.NonNull;

public class RecongitionRequest {

    private Recognition recognition;

    public RecongitionRequest(@NonNull String s3Path, boolean incognito) {
        this.recognition = new Recognition();
        this.recognition.s3_path = s3Path;
        this.recognition.incognito = incognito;
    }

    public static class Recognition {
        private String s3_path;
        private boolean incognito;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Recognition)) return false;

            Recognition that = (Recognition) o;

            if (incognito != that.incognito) return false;
            return s3_path != null ? s3_path.equals(that.s3_path) : that.s3_path == null;

        }

        @Override
        public int hashCode() {
            int result = s3_path != null ? s3_path.hashCode() : 0;
            result = 31 * result + (incognito ? 1 : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecongitionRequest)) return false;

        RecongitionRequest that = (RecongitionRequest) o;

        return recognition != null ? recognition.equals(that.recognition) : that.recognition == null;

    }

    @Override
    public int hashCode() {
        return recognition != null ? recognition.hashCode() : 0;
    }
}
