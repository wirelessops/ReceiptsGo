package co.smartreceipts.android.imports.intents.widget.info;

import androidx.annotation.NonNull;

import co.smartreceipts.android.imports.intents.model.FileType;

public interface IntentImportInformationView {

    /**
     * Presents a view to our user that informs them how to use this imported (eg via SEND intent
     * action) file type, when they have done this before
     *
     * @param fileType the {@link FileType} that is being imported
     */
    void presentIntentImportInformation(@NonNull FileType fileType);

    /**
     * Presents a fatal error to the user
     */
    void presentIntentImportFatalError();

}
