package com.wops.receiptsgo.receipts.delete;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import dagger.android.support.AndroidSupportInjection;

/**
 * A simple {@link DialogFragment} from which a user can confirm if he/she wishes to delete a receipt
 */
public class DeleteReceiptDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Inject
    ReceiptTableController receiptTableController;

    private Receipt receipt;

    public static DeleteReceiptDialogFragment newInstance(@NonNull Receipt receipt) {
        final DeleteReceiptDialogFragment dialogFragment = new DeleteReceiptDialogFragment();
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        Preconditions.checkNotNull(receipt, "A valid receipt must be included");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.delete_item, receipt.getName()))
                .setMessage(R.string.delete_sync_information)
                .setCancelable(true)
                .setPositiveButton(R.string.delete, this)
                .setNegativeButton(android.R.string.cancel, this)
                .show();
    }

    @Override
    public void onClick(@NonNull DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            receiptTableController.delete(receipt, new DatabaseOperationMetadata());
        }
        dismiss();
    }
}
