package co.smartreceipts.android.receipts.creator;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import io.reactivex.Observable;

/**
 * Provides a View contract from which a user can attempt to add a new receipt via the camera,
 * file import, or plain text.
 */
public interface ReceiptCreateActionView {

    /**
     * Informs our underlying UI that we should display our different receipt creation options menu
     * (ie adding a new receipt via the camera, file import, or plain text)
     */
    void displayReceiptCreationMenuOptions();

    /**
     * Informs our underlying UI that we should hide our different receipt creation options menu
     */
    void hideReceiptCreationMenuOptions();

    /**
     * Indicates our UI to route to a flow such that we can attempt to create a new receipt via the
     * camera app on the device
     */
    void createNewReceiptViaCamera();

    /**
     * Indicates our UI to route to a flow such that we can attempt to create a new receipt via
     * plain text input
     */
    void createNewReceiptViaPlainText();

    /**
     * Indicates our UI to route to a flow such that we can attempt to create a new receipt via the
     * file import/browser app on the device
     */
    void createNewReceiptViaFileImport();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever a user clicks a
     * button, indicating that he/she would like to see a list of available actions from which he/she
     * can create a new receipt
     */
    @NonNull
    Observable<Object> getCreateNewReceiptMenuButtonClicks();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever a user clicks a
     * button, indicating that he/she would like to dismiss a list of available receipt creation
     * actions
     */
    @NonNull
    Observable<Object> getDismissCreateNewReceiptMenuButtonClicks();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever a user clicks a
     * button, indicating the he/she would like to photograph a receipt
     */
    @NonNull
    Observable<Object> getCreateNewReceiptFromCameraButtonClicks();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever a user clicks a
     * button, indicating the he/she would like to import a receipt from an existing file on this
     * device
     */
    @NonNull
    Observable<Object> getCreateNewReceiptFromImportedFileButtonClicks();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever a user clicks a
     * button, indicating the he/she would like to create a receipt from plain text (ie without a
     * corresponding image/pdf file).
     */
    @NonNull
    Observable<Object> getCreateNewReceiptFromPlainTextButtonClicks();
}
