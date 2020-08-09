package co.smartreceipts.android.sync.widget.backups;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.databinding.DialogRenameBackupBinding;
import co.smartreceipts.core.sync.model.RemoteBackupMetadata;
import dagger.android.support.AndroidSupportInjection;

public class RenameRemoteBackupDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";

    @Inject
    NavigationHandler navigationHandler;

    private RemoteBackupMetadata backupMetadata;
    private EditText mRenameEditText;
    private ViewGroup container;
    private DialogRenameBackupBinding binding;

    public static RenameRemoteBackupDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        final RenameRemoteBackupDialogFragment fragment = new RenameRemoteBackupDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_BACKUP_METADATA, remoteBackupMetadata);
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
        backupMetadata = getArguments().getParcelable(ARG_BACKUP_METADATA);
        Preconditions.checkNotNull(backupMetadata, "This class requires that a RemoteBackupMetadata instance be provided");
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
        binding = DialogRenameBackupBinding.inflate(inflater, container, false);
        mRenameEditText = binding.dialogRenameFile;

        mRenameEditText.setText(backupMetadata.getSyncDeviceName());

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());
        builder.setTitle(R.string.rename);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.rename, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (mRenameEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.new_filename_empty_error), Toast.LENGTH_SHORT).show();
            } else {
                navigationHandler.showDialog(RenameRemoteBackupProgressDialogFragment.newInstance(backupMetadata, mRenameEditText.getText().toString().trim()));
                dismiss();
            }
        } else {
            dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
