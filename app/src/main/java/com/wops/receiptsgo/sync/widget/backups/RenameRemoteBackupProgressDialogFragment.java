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

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.ErrorEvent;
import co.smartreceipts.analytics.log.Logger;
import com.wops.receiptsgo.R;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.receiptsgo.sync.network.NetworkManager;
import co.smartreceipts.core.sync.model.RemoteBackupMetadata;
import co.smartreceipts.core.sync.provider.SyncProvider;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class RenameRemoteBackupProgressDialogFragment extends DialogFragment {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";
    private static final String ARG_BACKUP_NEW_NAME = "arg_backup_new_name";

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
    private String newFileName;

    public static RenameRemoteBackupProgressDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull String newFileNme) {
        final RenameRemoteBackupProgressDialogFragment fragment = new RenameRemoteBackupProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_BACKUP_METADATA, remoteBackupMetadata);
        args.putString(ARG_BACKUP_NEW_NAME, newFileNme);
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
        newFileName = getArguments().getString(ARG_BACKUP_NEW_NAME);
        Logger.info(this, "Renaming the backup of another device");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        if (backupMetadata != null) {
            dialog.setMessage(getString(R.string.dialog_remote_backup_rename_progress, backupMetadata.getSyncDeviceName(), newFileName));
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
        disposable = remoteBackupsDataCache.renameBackup(backupMetadata, newFileName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                    Logger.info(RenameRemoteBackupProgressDialogFragment.this, "Successfully handled rename of {}", backupMetadata);
                    Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_rename_toast_success), Toast.LENGTH_LONG).show();

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
                }, throwable -> {
                    analyticsManager.record(new ErrorEvent(RenameRemoteBackupProgressDialogFragment.this, throwable));
                    Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_rename_toast_failure), Toast.LENGTH_LONG).show();
                    dismiss();
                }, this::dismiss);
    }

    @Override
    public void onPause() {
        disposable.dispose();
        super.onPause();
    }
}
