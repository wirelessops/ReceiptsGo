package co.smartreceipts.android.sync.widget.backups;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import dagger.android.support.AndroidSupportInjection;

public class ImportRemoteBackupDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";

    @Inject
    NavigationHandler navigationHandler;

    private RemoteBackupMetadata mBackupMetadata;

    private CheckBox mOverwriteCheckBox;

    public static ImportRemoteBackupDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        final ImportRemoteBackupDialogFragment fragment = new ImportRemoteBackupDialogFragment();
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
        mBackupMetadata = getArguments().getParcelable(ARG_BACKUP_METADATA);
        Preconditions.checkNotNull(mBackupMetadata, "This class requires that a RemoteBackupMetadata instance be provided");
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_import_backup, null);
        mOverwriteCheckBox = (CheckBox) dialogView.findViewById(R.id.dialog_import_overwrite);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setTitle(getString(R.string.import_string_item, mBackupMetadata.getSyncDeviceName()));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_import_positive, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            navigationHandler.showDialog(ImportRemoteBackupWorkerProgressDialogFragment.newInstance(mBackupMetadata, mOverwriteCheckBox.isChecked()));
        }
        dismiss();
    }
}
