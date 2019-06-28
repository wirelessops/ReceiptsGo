package co.smartreceipts.android.sync.widget.backups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import dagger.android.support.AndroidSupportInjection;

public class AutomaticBackupsInfoDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.automatic_backups_info_dialog_message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.automatic_backups_info_dialog_positive, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            new NavigationHandler<>(getActivity(), new FragmentProvider()).navigateToBackupMenu();
        }
        dismiss();
    }
}
