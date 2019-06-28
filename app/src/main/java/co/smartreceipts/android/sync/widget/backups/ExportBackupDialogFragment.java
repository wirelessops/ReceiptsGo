package co.smartreceipts.android.sync.widget.backups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import dagger.android.support.AndroidSupportInjection;

public class ExportBackupDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

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
        builder.setTitle(R.string.dialog_export_title);
        builder.setMessage(R.string.dialog_export_text);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_export_positive, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            navigationHandler.showDialog(new ExportBackupWorkerProgressDialogFragment());
        }
        dismiss();
    }
}
