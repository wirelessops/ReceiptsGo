package co.smartreceipts.android.receipts.attacher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import dagger.android.support.AndroidSupportInjection;


public class ReceiptRemoveAttachmentDialogFragment extends DialogFragment {

    @Inject
    ReceiptTableController receiptTableController;

    private Receipt receipt;

    // TODO: 05.02.2018 remove attachment file manually. All files are saved to app's dir
    // while deleting receipt - it's deleting too
    // while removing attachment - no
    // TODO: 06.02.2018 check test for drive sync


    public static ReceiptRemoveAttachmentDialogFragment newInstance(@NonNull Receipt receipt) {
        final ReceiptRemoveAttachmentDialogFragment dialogFragment = new ReceiptRemoveAttachmentDialogFragment();
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
        return new AlertDialog.Builder(getActivity())
                .setTitle(receipt.getName())
                .setCancelable(true)
                .setMessage(getString(R.string.receipt_dialog_remove_attachment))
                .setNegativeButton(android.R.string.cancel, (dialogRemove, id) -> dialogRemove.cancel())
                .setPositiveButton(android.R.string.ok, (dialogRemove, which) -> receiptTableController
                        .update(receipt,
                                new ReceiptBuilderFactory(receipt).setFile(null).build(),
                                new DatabaseOperationMetadata()))
                .create();
    }
}
