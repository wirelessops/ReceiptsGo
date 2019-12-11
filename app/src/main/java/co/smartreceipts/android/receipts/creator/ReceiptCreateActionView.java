package co.smartreceipts.android.receipts.creator;

import androidx.annotation.NonNull;

import io.reactivex.Observable;
import kotlin.Unit;

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
     * @return an {@link Observable} that will emit an {@link Boolean} whenever a user clicks a
     * button, indicating that he/she is toggling the visibility of the list of available actions
     * from which he/she can create a new receipt. If the value is {@code true}, it indicates that
     * the menu was toggled open. A value of {@code false} will indicate that it was closed. Please
     * note that any subscription will also emit the current value (ie open vs closed)
     */
    @NonNull
    Observable<Boolean> getCreateNewReceiptMenuButtonToggles();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever a user clicks a
     * button, indicating the he/she would like to photograph a receipt
     */
    @NonNull
    Observable<Unit> getCreateNewReceiptFromCameraButtonClicks();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever a user clicks a
     * button, indicating the he/she would like to import a receipt from an existing file on this
     * device
     */
    @NonNull
    Observable<Unit> getCreateNewReceiptFromImportedFileButtonClicks();

    /**
     * @return an {@link Observable} that will emit an {@link Object} whenever a user clicks a
     * button, indicating the he/she would like to create a receipt from plain text (ie without a
     * corresponding image/pdf file).
     */
    @NonNull
    Observable<Unit> getCreateNewReceiptFromPlainTextButtonClicks();
}
