package com.wops.receiptsgo.sync.widget.backups;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.databinding.DialogImportBackupBinding;
import dagger.android.support.AndroidSupportInjection;

public class ImportLocalBackupDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARG_SMR_URI = "arg_smr_uri";

    @Inject
    NavigationHandler navigationHandler;

    private Uri mUri;
    private CheckBox mOverwriteCheckBox;
    private ViewGroup container;
    private DialogImportBackupBinding binding;

    public static ImportLocalBackupDialogFragment newInstance(@NonNull Uri uri) {
        final ImportLocalBackupDialogFragment fragment = new ImportLocalBackupDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_SMR_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(ARG_SMR_URI);
        Preconditions.checkNotNull(mUri, "ImportBackupDialogFragment requires a valid SMR Uri");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        binding = DialogImportBackupBinding.inflate(inflater, container, false);
        mOverwriteCheckBox = binding.dialogImportOverwrite;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());
        builder.setTitle(R.string.import_string);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_import_positive, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            navigationHandler.showDialog(ImportLocalBackupWorkerProgressDialogFragment.newInstance(mUri, mOverwriteCheckBox.isChecked()));
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
