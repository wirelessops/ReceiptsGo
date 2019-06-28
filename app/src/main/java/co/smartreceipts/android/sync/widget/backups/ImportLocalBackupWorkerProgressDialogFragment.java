package co.smartreceipts.android.sync.widget.backups;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.sync.manual.ManualRestoreTask;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;


public class ImportLocalBackupWorkerProgressDialogFragment extends DialogFragment {

    private static final String ARG_SMR_URI = "arg_smr_uri";
    private static final String ARG_OVERWRITE = "arg_overwrite";

    @Inject
    PersistenceManager persistenceManager;

    @Inject
    Analytics analytics;

    @Inject
    TripTableController tripTableController;

    @Inject
    ManualRestoreTask manualRestoreTask;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Uri uri;
    private boolean overwrite;

    public static ImportLocalBackupWorkerProgressDialogFragment newInstance(@NonNull Uri uri, boolean overwrite) {
        final ImportLocalBackupWorkerProgressDialogFragment fragment = new ImportLocalBackupWorkerProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_SMR_URI, uri);
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
        uri = getArguments().getParcelable(ARG_SMR_URI);
        overwrite = getArguments().getBoolean(ARG_OVERWRITE);
        Preconditions.checkNotNull(uri, "ImportBackupDialogFragment requires a valid SMR Uri");
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
    public void onStart() {
        super.onStart();
        compositeDisposable.add(manualRestoreTask.restoreData(uri, overwrite)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    manualRestoreTask.markRestorationAsComplete(uri, overwrite);
                    for (final Table table : persistenceManager.getDatabase().getTables()) {
                        table.clearCache();
                    }
                    tripTableController.get();
                    if (getActivity() != null) {
                        getActivity().finishAffinity();
                    }
                    Toast.makeText(getActivity(), R.string.toast_import_complete, Toast.LENGTH_LONG).show();
                    this.dismiss();
                }, throwable -> {
                    analytics.record(new ErrorEvent(ImportLocalBackupWorkerProgressDialogFragment.this, throwable));
                    Toast.makeText(getActivity(), getString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
                    dismiss();
                }));
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
