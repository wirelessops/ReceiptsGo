package co.smartreceipts.android.imports;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.storage.StorageManager;

public class FileImportProcessorFactory {

    private final Context context;
    private final UserPreferenceManager preferenceManager;
    private final StorageManager storageManager;

    @Inject
    FileImportProcessorFactory(Context context, UserPreferenceManager userPreferenceManager, StorageManager storageManager) {
        this.context = context;
        this.preferenceManager = userPreferenceManager;
        this.storageManager = storageManager;
    }

    @NonNull
    public FileImportProcessor get(int requestCode, @NonNull Trip trip) {
        if (RequestCodes.PHOTO_REQUESTS.contains(requestCode)) {
            return new ImageImportProcessor(trip, storageManager, preferenceManager, context);
        }

        if (RequestCodes.PDF_REQUESTS.contains(requestCode)) {
            return new GenericFileImportProcessor(trip, storageManager, context);
        }

        return new AutoFailImportProcessor();
    }
}
