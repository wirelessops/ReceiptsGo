package co.smartreceipts.android.receipts.attacher;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.imports.CameraInteractionController;
import co.smartreceipts.android.imports.RequestCodes;

@ApplicationScope
public class ReceiptAttachmentManager {

    private final Analytics analytics;

    @Inject
    ReceiptAttachmentManager(Analytics analytics) {
        this.analytics = analytics;
    }

    @NonNull
    public Uri attachPhoto(@NonNull Fragment fragment) {
        analytics.record(Events.Receipts.ReceiptAttachPhoto);
        return new CameraInteractionController(fragment).addPhoto();
    }

    public boolean attachPicture(@NonNull Fragment fragment, boolean newReceipt) {
        analytics.record(newReceipt ? Events.Receipts.ReceiptImportImage : Events.Receipts.ReceiptAttachPicture);

        // @see https://developer.android.com/guide/topics/providers/document-provider.html#client
        // Use ACTION_GET_CONTENT instead of ACTION_OPEN_DOCUMENT as this is simply a read/import
        try {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            fragment.startActivityForResult(intent, newReceipt ? RequestCodes.NEW_RECEIPT_IMPORT_IMAGE : RequestCodes.ATTACH_GALLERY_IMAGE);
        } catch (ActivityNotFoundException ex) {
            return false;
        }
        return true;
    }

    public boolean attachFile(@NonNull Fragment fragment, boolean newReceipt) {
        analytics.record(newReceipt ? Events.Receipts.ReceiptImportPdf : Events.Receipts.ReceiptAttachFile);

        try {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            fragment.startActivityForResult(intent, newReceipt ? RequestCodes.NEW_RECEIPT_IMPORT_PDF : RequestCodes.ATTACH_GALLERY_PDF);
        } catch (ActivityNotFoundException ex) {
            return false;
        }
        return true;
    }
}
