package com.wops.receiptsgo.sync.widget.backups;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.ErrorEvent;
import com.wops.receiptsgo.R;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.errors.SyncErrorType;
import com.wops.core.sync.model.RemoteBackupMetadata;
import com.wops.receiptsgo.sync.network.NetworkManager;
import com.wops.core.sync.provider.SyncProvider;
import com.wops.analytics.log.Logger;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class DeleteRemoteBackupProgressDialogFragment extends DialogFragment {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";

    @Inject
    DatabaseHelper database;
    @Inject
    NetworkManager networkManager;
    @Inject
    Analytics analyticsManager;
    @Inject
    BackupProvidersManager backupProvidersManager;

    private RemoteBackupsDataCache remoteBackupsDataCache;
    private Disposable disposable;

    private RemoteBackupMetadata backupMetadata;

    public static DeleteRemoteBackupProgressDialogFragment newInstance() {
        return newInstance(null);
    }

    public static DeleteRemoteBackupProgressDialogFragment newInstance(@Nullable RemoteBackupMetadata remoteBackupMetadata) {
        final DeleteRemoteBackupProgressDialogFragment fragment = new DeleteRemoteBackupProgressDialogFragment();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        backupMetadata = getArguments().getParcelable(ARG_BACKUP_METADATA);
        if (backupMetadata == null) {
            Logger.info(this, "Deleting the local device backup");
        } else {
            Logger.info(this, "Deleting the backup of another device");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        if (backupMetadata != null) {
            dialog.setMessage(getString(R.string.dialog_remote_backup_delete_progress, backupMetadata.getSyncDeviceName()));
        } else {
            dialog.setMessage(getString(R.string.dialog_remote_backup_restore_progress));
        }
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        remoteBackupsDataCache = new RemoteBackupsDataCache(getFragmentManager(), getContext(),
                backupProvidersManager, networkManager, database);
    }

    @Override
    public void onResume() {
        super.onResume();
        disposable = remoteBackupsDataCache.deleteBackup(backupMetadata)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteSuccess -> {
                    if (deleteSuccess) {
                        Logger.info(DeleteRemoteBackupProgressDialogFragment.this, "Successfully handled delete of {}", backupMetadata);
                        if (backupMetadata != null) {
                            Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_delete_toast_success), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_restore_toast_success), Toast.LENGTH_LONG).show();
                            backupProvidersManager.markErrorResolved(SyncErrorType.DriveRecoveryRequired);
                        }

                        // Note: this is kind of hacky but should work
                        remoteBackupsDataCache.clearGetBackupsResults();
                        final Fragment uncastedBackupsFragment = getFragmentManager().findFragmentByTag(BackupsFragment.class.getName());
                        if (uncastedBackupsFragment instanceof BackupsFragment) {
                            // If we're active, kick off a refresh directly in the fragment
                            final BackupsFragment backupsFragment = (BackupsFragment) uncastedBackupsFragment;
                            backupsFragment.updateViewsForProvider(SyncProvider.GoogleDrive);
                        } else {
                            // Kick off a refresh, so we catch it next time
                            remoteBackupsDataCache.getBackups(SyncProvider.GoogleDrive);
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_delete_toast_failure), Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    analyticsManager.record(new ErrorEvent(DeleteRemoteBackupProgressDialogFragment.this, throwable));
                    if (backupMetadata != null) {
                        Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_delete_toast_failure), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_restore_toast_failure), Toast.LENGTH_LONG).show();
                    }

                    dismiss();
                }, this::dismiss);
    }

    @Override
    public void onPause() {
        disposable.dispose();
        super.onPause();
    }
}
