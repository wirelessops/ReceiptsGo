package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.widget.Toast;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.receipts.attacher.ReceiptAttachmentManager;
import dagger.android.support.AndroidSupportInjection;

public class ImportPhotoPdfDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = ImportPhotoPdfDialogFragment.class.getSimpleName();

    private final int WHICH_IMAGE = 0;
    private final int WHICH_PDF = 1;

    @Inject
    ReceiptAttachmentManager receiptAttachmentManager;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String image = getString(R.string.image);
        final String pdf = getString(R.string.pdf);
        final CharSequence[] choices = new CharSequence[] {image, pdf};
        builder.setItems(choices, this);
        builder.setCancelable(true);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        boolean attachmentResult = false;

        if (which == WHICH_IMAGE) {
            attachmentResult = receiptAttachmentManager.attachPicture(getParentFragment(), true);
        } else if (which == WHICH_PDF) {
            attachmentResult = receiptAttachmentManager.attachFile(getParentFragment(), true);
        }

        if (!attachmentResult) {
            Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }
}
