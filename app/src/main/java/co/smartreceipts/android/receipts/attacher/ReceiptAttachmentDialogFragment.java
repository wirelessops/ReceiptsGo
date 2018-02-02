package co.smartreceipts.android.receipts.attacher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import dagger.android.support.AndroidSupportInjection;


public class ReceiptAttachmentDialogFragment extends DialogFragment {

    @Inject
    ReceiptAttachmentManager receiptAttachmentManager;

    private Receipt receipt;


    public static ReceiptAttachmentDialogFragment newInstance(@NonNull Receipt receipt) {
        final ReceiptAttachmentDialogFragment dialogFragment = new ReceiptAttachmentDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Receipt.PARCEL_KEY, receipt);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        Preconditions.checkNotNull(receipt, "ReceiptAttachmentDialogFragment requires a valid Receipt");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Fragment targetFragment = getTargetFragment();

        if (targetFragment == null || !(targetFragment instanceof Listener)) {
            throw new IllegalStateException("Target fragment must implement ReceiptAttachmentDialogFragment.Listener interface");
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(receipt.getName())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_receipt_attachment, null);
        dialogBuilder.setView(dialogView);

        final AlertDialog dialog = dialogBuilder.create();

        dialogView.findViewById(R.id.attach_photo).setOnClickListener(v -> {
            ((Listener) targetFragment).setImageUri(receiptAttachmentManager.attachPhoto(getTargetFragment()));
            dialog.cancel();
        });

        dialogView.findViewById(R.id.attach_picture).setOnClickListener(v -> {
            if (!receiptAttachmentManager.attachPicture(getTargetFragment())) {
                Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
            }
            dialog.cancel();
        });

        dialogView.findViewById(R.id.attach_file).setOnClickListener(v -> {
            if (!receiptAttachmentManager.attachFile(this)) {
                Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
            }
            dialog.cancel();
        });

        return dialog;

    }

    public interface Listener {
        void setImageUri(Uri uri);
    }
}
