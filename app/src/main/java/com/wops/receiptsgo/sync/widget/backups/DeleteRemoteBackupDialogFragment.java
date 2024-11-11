package com.wops.receiptsgo.sync.widget.backups;

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
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.model.RemoteBackupMetadata;
import dagger.android.support.AndroidSupportInjection;

public class DeleteRemoteBackupDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";

    @Inject
    BackupProvidersManager backupProvidersManager;

    @Inject
    NavigationHandler navigationHandler;

    private RemoteBackupMetadata backupMetadata;

    public static DeleteRemoteBackupDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        final DeleteRemoteBackupDialogFragment fragment = new DeleteRemoteBackupDialogFragment();
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete);
        if (backupMetadata.getSyncDeviceId().equals(backupProvidersManager.getDeviceSyncId())) {
            builder.setMessage(getString(R.string.dialog_remote_backup_delete_message_this_device));
        } else {
            builder.setMessage(getString(R.string.dialog_remote_backup_delete_message, backupMetadata.getSyncDeviceName()));
        }
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.delete, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            navigationHandler.showDialog(DeleteRemoteBackupProgressDialogFragment.newInstance(backupMetadata));
        }
        dismiss();
    }
}
