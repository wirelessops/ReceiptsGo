package com.wops.receiptsgo.imports.intents.model;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class IntentImportResult {

    private final Uri uri;
    private final FileType fileType;

    public IntentImportResult(@NonNull Uri uri, @NonNull FileType fileType) {
        this.uri = Preconditions.checkNotNull(uri);
        this.fileType = Preconditions.checkNotNull(fileType);
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }

    @NonNull
    public FileType getFileType() {
        return fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntentImportResult)) return false;

        IntentImportResult that = (IntentImportResult) o;

        if (!uri.equals(that.uri)) return false;
        return fileType == that.fileType;

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + fileType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "IntentImportResult{" +
                "uri=" + uri +
                ", fileType=" + fileType +
                '}';
    }
}
