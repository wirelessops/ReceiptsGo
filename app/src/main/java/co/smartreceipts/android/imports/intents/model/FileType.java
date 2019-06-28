package co.smartreceipts.android.imports.intents.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;

public enum FileType {

    Image("jpg", "jpeg", "png"),
    Pdf("pdf"),
    Smr("smr");

    private final List<String> supportedExtensions;

    FileType(@NonNull String... supportedExtensions) {
        this.supportedExtensions = Arrays.asList(Preconditions.checkNotNull(supportedExtensions));
    }

    public boolean supportsExtension(@NonNull String extension) {
        return supportedExtensions.contains(extension);
    }

    @Nullable
    public static FileType getFileTypeFromExtension(@Nullable String extension) {
        if (extension != null) {
            for (final FileType fileType : FileType.values()) {
                if (fileType.supportsExtension(extension)) {
                    return fileType;
                }
            }
        }
        return null;
    }
}
