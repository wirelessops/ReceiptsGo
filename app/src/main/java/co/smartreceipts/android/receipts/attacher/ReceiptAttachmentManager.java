package co.smartreceipts.android.receipts.attacher;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

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

    public Uri attachPhoto(Fragment fragment) {
        analytics.record(Events.Receipts.ReceiptAttachPhoto);
        return new CameraInteractionController(fragment).addPhoto();
    }

    public boolean attachPicture(Fragment fragment) {
        analytics.record(Events.Receipts.ReceiptAttachPicture);

        try {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            fragment.startActivityForResult(intent, RequestCodes.ATTACH_GALLERY_IMAGE);
        } catch (ActivityNotFoundException ex) {
            return false;
        }
        return true;
    }

    public boolean attachFile(Fragment fragment) {
        analytics.record(Events.Receipts.ReceiptAttachFile);

        try {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            fragment.startActivityForResult(intent, RequestCodes.ATTACH_GALLERY_PDF);
        } catch (ActivityNotFoundException ex) {
            return false;
        }
        return true;
    }
}
