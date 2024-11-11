package com.wops.receiptsgo.ocr.apis.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OcrResponseField<T> implements Serializable {

    @SerializedName("data")
    private final T data;

    @SerializedName("confidenceLevel")
    private final Double confidenceLevel;

    public OcrResponseField(T data, Double confidenceLevel) {
        this.data = data;
        this.confidenceLevel = confidenceLevel;
    }

    @Nullable
    public T getData() {
        return data;
    }

    public double getConfidenceLevel() {
        return confidenceLevel != null ? confidenceLevel : 0;
    }

    @Override
    public String toString() {
        return "OcrResponseField{" +
                "data=" + data +
                ", confidenceLevel=" + confidenceLevel +
                '}';
    }
}
