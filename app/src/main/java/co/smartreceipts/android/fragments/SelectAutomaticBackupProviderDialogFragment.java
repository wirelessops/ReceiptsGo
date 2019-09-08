package co.smartreceipts.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;
import dagger.android.support.AndroidSupportInjection;

public class SelectAutomaticBackupProviderDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Inject
    BackupProvidersManager backupProvidersManager;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_automatic_backup_provider, null);
        final RadioGroup providerGroup = dialogView.findViewById(R.id.automatic_backup_provider_radiogroup);
        final RadioButton noneProviderButton = providerGroup.findViewById(R.id.automatic_backup_provider_none);
        final RadioButton googleDriveProviderButton = providerGroup.findViewById(R.id.automatic_backup_provider_google_drive);

        if (backupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
            googleDriveProviderButton.setChecked(true);
        } else {
            noneProviderButton.setChecked(true);
        }
        providerGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            final SyncProvider currentProvider = backupProvidersManager.getSyncProvider();
            final SyncProvider newProvider;
            if (id == R.id.automatic_backup_provider_google_drive) {
                newProvider = SyncProvider.GoogleDrive;
            } else {
                newProvider = SyncProvider.None;
            }
            if (currentProvider != newProvider) {
                backupProvidersManager.setAndInitializeSyncProvider(newProvider, getActivity());
            }
            dismiss();
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        dismiss();
    }
}
