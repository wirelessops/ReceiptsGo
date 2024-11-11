package com.wops.receiptsgo.sync.widget.backups;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.ErrorEvent;
import com.wops.receiptsgo.R;
import com.wops.receiptsgo.persistence.PersistenceManager;
import com.wops.receiptsgo.sync.manual.ManualBackupTask;
import com.wops.receiptsgo.utils.IntentUtils;
import com.wops.receiptsgo.widget.tooltip.report.backup.data.BackupReminderTooltipStorage;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class ExportBackupWorkerProgressDialogFragment extends DialogFragment {

    @Inject
    PersistenceManager persistenceManager;

    @Inject
    Analytics analytics;

    @Inject
    BackupReminderTooltipStorage backupReminderTooltipStorage;

    @Inject
    ManualBackupTask manualBackupTask;

    private Disposable disposable;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(getString(R.string.dialog_export_working));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        disposable = manualBackupTask.backupData().observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                    final Intent intent = IntentUtils.getSendIntent(getContext(), file);
                    getActivity().startActivity(Intent.createChooser(intent, getString(R.string.export)));
                    backupReminderTooltipStorage.setLastManualBackupDate();
                    manualBackupTask.markBackupAsComplete();
                }, throwable -> {
                    analytics.record(new ErrorEvent(ExportBackupWorkerProgressDialogFragment.this, throwable));
                    Toast.makeText(getContext(), getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
                    dismiss();
                }, this::dismiss);
    }

    @Override
    public void onPause() {
        disposable.dispose();
        super.onPause();
    }
}
