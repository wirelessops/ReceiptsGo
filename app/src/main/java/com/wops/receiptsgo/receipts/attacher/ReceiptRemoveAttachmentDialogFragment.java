package com.wops.receiptsgo.receipts.attacher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.factory.ReceiptBuilderFactory;
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import dagger.android.support.AndroidSupportInjection;


public class ReceiptRemoveAttachmentDialogFragment extends DialogFragment {

    @Inject
    ReceiptTableController receiptTableController;

    private Receipt receipt;


    public static ReceiptRemoveAttachmentDialogFragment newInstance(@NonNull Receipt receipt) {
        final ReceiptRemoveAttachmentDialogFragment dialogFragment = new ReceiptRemoveAttachmentDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Receipt.PARCEL_KEY, receipt);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
