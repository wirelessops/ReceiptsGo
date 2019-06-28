package co.smartreceipts.android.imports;

import android.net.Uri;
import androidx.annotation.NonNull;

import java.io.File;

import io.reactivex.Single;

public interface FileImportProcessor {

    /**
     * Processes a uri to get an Android file
     * @param uri the desired {@link Uri}
     * @return the resultant {@link File}
     */
    @NonNull
    Single<File> process(@NonNull Uri uri);
}
