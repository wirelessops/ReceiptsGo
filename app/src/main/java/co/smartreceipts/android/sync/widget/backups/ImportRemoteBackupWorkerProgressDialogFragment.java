package co.smartreceipts.android.sync.widget.backups;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.ErrorEvent;
import co.smartreceipts.android.R;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.MissingFilesException;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.network.NetworkManager;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ImportRemoteBackupWorkerProgressDialogFragment extends DialogFragment {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";
    private static final String ARG_OVERWRITE = "arg_overwrite";

    @Inject
    DatabaseHelper database;
    @Inject
    NetworkManager networkManager;
    @Inject
    Analytics analytics;
    @Inject
    TripTableController tripTableController;
    @Inject
    BackupProvidersManager backupProvidersManager;

    private RemoteBackupsDataCache remoteBackupsDataCache;
    private Disposable disposable;

    private RemoteBackupMetadata backupMetadata;
    private boolean overwrite;

    public static ImportRemoteBackupWorkerProgressDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean overwrite) {
        final ImportRemoteBackupWorkerProgressDialogFragment fragment = new ImportRemoteBackupWorkerProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_BACKUP_METADATA, remoteBackupMetadata);
        args.putBoolean(ARG_OVERWRITE, overwrite);
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
        overwrite = getArguments().getBoolean(ARG_OVERWRITE);
        Preconditions.checkNotNull(backupMetadata, "This class requires that a RemoteBackupMetadata instance be provided");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(getString(R.string.progress_import));
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
        disposable = remoteBackupsDataCache.restoreBackup(backupMetadata, overwrite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success != null && success) {
                        Toast.makeText(getActivity(), R.string.toast_import_complete, Toast.LENGTH_LONG).show();
                        for (final Table table : database.getTables()) {
                            table.clearCache();
                        }
                        tripTableController.get();
                        getActivity().finishAffinity(); // TODO: Fix this hack (for the settings import)
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    analytics.record(new ErrorEvent(ImportRemoteBackupWorkerProgressDialogFragment.this, throwable));
                    if (throwable instanceof MissingFilesException) {
                        Toast.makeText(getActivity(), getString(R.string.IMPORT_MISSING_ERROR), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
                    }
                    remoteBackupsDataCache.removeCachedRestoreBackupFor(backupMetadata);
                    dismiss();
                }, () -> {
                    remoteBackupsDataCache.removeCachedRestoreBackupFor(backupMetadata);
                    dismiss();
                });
    }

    @Override
    public void onPause() {
        disposable.dispose();
        super.onPause();
    }
}
