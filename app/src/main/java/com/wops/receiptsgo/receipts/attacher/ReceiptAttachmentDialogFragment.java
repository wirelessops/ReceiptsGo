package com.wops.receiptsgo.receipts.attacher;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.databinding.DialogReceiptAttachmentBinding;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.widget.dialog.BaseBottomSheetDialog;
import dagger.android.support.AndroidSupportInjection;

public class ReceiptAttachmentDialogFragment extends BaseBottomSheetDialog {

    @Inject
    ReceiptAttachmentManager receiptAttachmentManager;

    private Receipt receipt;
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
        final Fragment parentFragment = getParentFragment();

        if (!(parentFragment instanceof Listener)) {
            throw new IllegalStateException("Parent fragment must implement ReceiptAttachmentDialogFragment.Listener interface");
        }

        binding = DialogReceiptAttachmentBinding.inflate(inflater, container, false);

        binding.receiptName.setText(receipt.getName());

        binding.attachPhoto.setOnClickListener(v -> {
            ((Listener) parentFragment).setImageUri(receiptAttachmentManager.attachPhoto(parentFragment));
            dismiss();
        });

        binding.attachPicture.setOnClickListener(v -> {
            if (!receiptAttachmentManager.attachPicture(parentFragment, false)) {
                Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        binding.attachFile.setOnClickListener(v -> {
            if (!receiptAttachmentManager.attachFile(parentFragment, false)) {
                Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        return binding.getRoot();
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
