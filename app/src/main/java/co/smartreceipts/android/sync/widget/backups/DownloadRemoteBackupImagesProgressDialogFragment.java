package co.smartreceipts.android.sync.widget.backups;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class DownloadRemoteBackupImagesProgressDialogFragment extends DialogFragment {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";
    private static final String ARG_DOWNLOAD_DEBUG_MODE = "arg_download_debug_mode";

    @Inject
    DatabaseHelper database;
    @Inject
    NetworkManager networkManager;
    @Inject
    Analytics analytics;
    @Inject
    BackupProvidersManager backupProvidersManager;

    private RemoteBackupsDataCache remoteBackupsDataCache;
    private Disposable disposable;

    private RemoteBackupMetadata backupMetadata;
    private boolean debugMode;

    public static DownloadRemoteBackupImagesProgressDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        return newInstance(remoteBackupMetadata, false);
    }

    public static DownloadRemoteBackupImagesProgressDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean debugMode) {
        final DownloadRemoteBackupImagesProgressDialogFragment fragment = new DownloadRemoteBackupImagesProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_BACKUP_METADATA, remoteBackupMetadata);
        args.putBoolean(ARG_DOWNLOAD_DEBUG_MODE, debugMode);
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
        debugMode = getArguments().getBoolean(ARG_DOWNLOAD_DEBUG_MODE);
        Preconditions.checkNotNull(backupMetadata, "This class requires that a RemoteBackupMetadata instance be provided");
        Logger.info(this, "Initializing download of [{}] in debug mode == {}", backupMetadata, debugMode);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(getString(R.string.dialog_remote_backup_download_progress));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SmartReceiptsApplication smartReceiptsApplication = ((SmartReceiptsApplication)getActivity().getApplication());
        remoteBackupsDataCache = new RemoteBackupsDataCache(getFragmentManager(), getContext(),
                backupProvidersManager, networkManager,
                database);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Observable downloadObservable;

        disposable = remoteBackupsDataCache.downloadBackup(backupMetadata, debugMode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(zippedDataFile -> {
                    if (zippedDataFile != null) {
                        final Intent intent = IntentUtils.getSendIntent(getContext(), zippedDataFile);
                        getActivity().startActivity(Intent.createChooser(intent, getString(R.string.export)));
                    } else {
                        Toast.makeText(getContext(), getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    analytics.record(new ErrorEvent(DownloadRemoteBackupImagesProgressDialogFragment.this, throwable));
                    Toast.makeText(getContext(), getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
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
