package co.smartreceipts.android.receipts.attacher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.databinding.DialogReceiptAttachmentBinding;
import co.smartreceipts.android.model.Receipt;
import dagger.android.support.AndroidSupportInjection;

public class ReceiptAttachmentDialogFragment extends DialogFragment {

    @Inject
    ReceiptAttachmentManager receiptAttachmentManager;

    private Receipt receipt;
    private ViewGroup container;
    private DialogReceiptAttachmentBinding binding;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Fragment parentFragment = getParentFragment();

        if (!(parentFragment instanceof Listener)) {
            throw new IllegalStateException("Parent fragment must implement ReceiptAttachmentDialogFragment.Listener interface");
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(receipt.getName())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());

        binding = DialogReceiptAttachmentBinding.inflate(getActivity().getLayoutInflater(), container, false);
        dialogBuilder.setView(binding.getRoot());

        final AlertDialog dialog = dialogBuilder.create();

        binding.attachPhoto.setOnClickListener(v -> {
            ((Listener) parentFragment).setImageUri(receiptAttachmentManager.attachPhoto(parentFragment));
            dialog.dismiss();
        });

        binding.attachPicture.setOnClickListener(v -> {
            if (!receiptAttachmentManager.attachPicture(parentFragment, false)) {
                Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        binding.attachFile.setOnClickListener(v -> {
            if (!receiptAttachmentManager.attachFile(parentFragment, false)) {
                Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        return dialog;
    }

    public interface Listener {
        void setImageUri(Uri uri);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
