package co.smartreceipts.android.sync.widget.errors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupProgressDialogFragment;
import dagger.android.support.AndroidSupportInjection;

public class DriveRecoveryDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Inject
    NavigationHandler navigationHandler;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_remote_backup_drive_restore_title);
        builder.setMessage(R.string.dialog_remote_backup_drive_restore_message);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            navigationHandler.showDialog(DeleteRemoteBackupProgressDialogFragment.newInstance());
        }
        dismiss();
    }
}
