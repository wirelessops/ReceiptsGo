package com.wops.receiptsgo.receipts.creator;

/**
 * Provides a View contract from which a user can attempt to add a new receipt via the camera,
 * file import, image import, or plain text.
 */
public interface ReceiptCreateActionView {

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
     * Indicates our UI to route to a flow such that we can attempt to create a new receipt via the
     * image gallery app on the device
     */
    void createNewReceiptViaImageImport();
}
