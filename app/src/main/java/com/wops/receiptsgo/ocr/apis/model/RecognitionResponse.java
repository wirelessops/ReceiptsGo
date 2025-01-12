package com.wops.receiptsgo.ocr.apis.model;

import androidx.annotation.Nullable;

import java.util.Date;

public class RecognitionResponse {

    private Recognition recognition;

    @Nullable
    public Recognition getRecognition() {
        return recognition;
    }

    public static class Recognition {

        private String id;
        private String status;
        private String s3_path;
        private RecognitionData data;
        private Date created_at;

        @Nullable
        public String getId() {
            return id;
        }

        @Nullable
        public String getStatus() {
            return status;
        }

        @Nullable
        public String getS3Path() {
            return s3_path;
        }

        @Nullable
        public RecognitionData getData() {
            return data;
        }

        public Date getCreatedAt() {
            return created_at;
        }

    }

    public static class RecognitionData {

        private OcrResponse recognition_data;

        @Nullable
        public OcrResponse getRecognitionData() {
            return recognition_data;
        }
    }
}
