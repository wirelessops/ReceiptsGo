package co.smartreceipts.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.databinding.DialogSelectAutomaticBackupProviderBinding;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.core.sync.provider.SyncProvider;
import dagger.android.support.AndroidSupportInjection;

public class SelectAutomaticBackupProviderDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Inject
    BackupProvidersManager backupProvidersManager;

    private ViewGroup container;
    private DialogSelectAutomaticBackupProviderBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
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
        binding = DialogSelectAutomaticBackupProviderBinding.inflate(inflater, container, false);
        final RadioGroup providerGroup = binding.automaticBackupProviderRadiogroup;
        final RadioButton noneProviderButton = binding.automaticBackupProviderNone;
        final RadioButton googleDriveProviderButton = binding.automaticBackupProviderGoogleDrive;

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
        builder.setView(binding.getRoot());
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
