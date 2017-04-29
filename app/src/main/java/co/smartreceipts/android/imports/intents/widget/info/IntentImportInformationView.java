package co.smartreceipts.android.imports.intents.widget.info;

import android.support.annotation.NonNull;

import co.smartreceipts.android.imports.intents.model.FileType;

public interface IntentImportInformationView {

    /**
     * Presents a view to our user that informs them how to use this imported (eg via SEND intent
     * action) file type, when this is the first time the user has performed this action
     *
     * @param fileType the {@link FileType} that is being imported
     */
    void presentFirstTimeInformation(@NonNull FileType fileType);

    /**
     * Presents a view to our user that informs them how to use this imported (eg via SEND intent
     * action) file type, when they have done this before
     *
     * @param fileType the {@link FileType} that is being imported
     */
    void presentGenericImportInformation(@NonNull FileType fileType);

    /**
     * Presents a fatal error to the user
     */
    void presentFatalError();

}
