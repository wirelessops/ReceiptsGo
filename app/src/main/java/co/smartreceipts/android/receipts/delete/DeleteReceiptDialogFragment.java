package co.smartreceipts.android.receipts.delete;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
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
    public void onAttach(Context context) {
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.delete_item, receipt.getName()));
        builder.setMessage(R.string.delete_sync_information);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.delete, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.show();
    }

    @Override
    public void onClick(@NonNull DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            receiptTableController.delete(receipt, new DatabaseOperationMetadata());
        }
        dismiss();
    }
}
