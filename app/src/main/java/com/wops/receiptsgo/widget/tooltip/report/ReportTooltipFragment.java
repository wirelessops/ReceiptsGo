package com.wops.receiptsgo.widget.tooltip.report;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.widget.tooltip.LegacyTooltipView;
import com.wops.receiptsgo.widget.tooltip.Tooltip;
import com.wops.receiptsgo.widget.tooltip.report.backup.BackupNavigator;
import com.wops.receiptsgo.widget.tooltip.report.generate.GenerateNavigator;
import com.wops.core.sync.errors.SyncErrorType;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static android.view.View.GONE;
import static com.wops.receiptsgo.widget.tooltip.report.ReportTooltipUiIndicator.State.BackupReminder;
import static com.wops.receiptsgo.widget.tooltip.report.ReportTooltipUiIndicator.State.GenerateInfo;
import static com.wops.receiptsgo.widget.tooltip.report.ReportTooltipUiIndicator.State.ImportInfo;
import static com.wops.receiptsgo.widget.tooltip.report.ReportTooltipUiIndicator.State.None;
import static com.wops.receiptsgo.widget.tooltip.report.ReportTooltipUiIndicator.State.SyncError;


public class ReportTooltipFragment extends Fragment implements LegacyTooltipView {

    @Inject
    ReportTooltipPresenter presenter;

    private Tooltip tooltip;

    private final PublishSubject<ReportTooltipUiIndicator> tooltipClickStream = PublishSubject.create();
    private final PublishSubject<ReportTooltipUiIndicator> closeClickStream = PublishSubject.create();

    @NonNull
    public static ReportTooltipFragment newInstance() {
        return new ReportTooltipFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!(this.getParentFragment() instanceof GenerateNavigator)) {
            throw new IllegalStateException("Parent fragment must implement GenerateNavigator interface");
        }

        if (!(this.getParentFragment() instanceof BackupNavigator)) {
            throw new IllegalStateException("Parent fragment must implement BackupNavigator interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tooltip = new Tooltip(getContext());
        tooltip.setVisibility(GONE);
        return tooltip;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.subscribe();
    }

    @Override
    public void onPause() {
        presenter.unsubscribe();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tooltip = null;
    }

    @Override
    public void present(ReportTooltipUiIndicator uiIndicator) {
        if (uiIndicator.getState() == SyncError) {
            presentError(uiIndicator.getErrorType().get());
        } else if (uiIndicator.getState() == GenerateInfo) {
            presentGenerateInfo();
        } else if (uiIndicator.getState() == BackupReminder) {
            presentBackupReminder(uiIndicator.getDaysSinceBackup().get());
        } else if (uiIndicator.getState() == ImportInfo) {
            presentImportInfo();
        } else if (uiIndicator.getState() == None) {
            tooltip.hideWithAnimation();
        }
    }

    @NonNull
    @Override
    public Observable<ReportTooltipUiIndicator> getCloseButtonClicks() {
        return closeClickStream;
    }

    @NonNull
    @Override
    public Observable<ReportTooltipUiIndicator> getTooltipsClicks() {
        return tooltipClickStream;
    }

    private void presentError(@NonNull final SyncErrorType syncErrorType) {
        if (syncErrorType == SyncErrorType.UserRevokedRemoteRights) {
            tooltip.setErrorWithoutClose(R.string.drive_sync_error_no_permissions, view ->
                    tooltipClickStream.onNext(ReportTooltipUiIndicator.syncError(syncErrorType)));
        } else if (syncErrorType == SyncErrorType.DriveRecoveryRequired) {
            tooltip.setErrorWithoutClose(R.string.drive_sync_error_lost_data, view -> {
                tooltipClickStream.onNext(ReportTooltipUiIndicator.syncError(syncErrorType));
            });
        } else if (syncErrorType == SyncErrorType.NoRemoteDiskSpace) {
            tooltip.setError(R.string.drive_sync_error_no_space, view -> {
                closeClickStream.onNext(ReportTooltipUiIndicator.syncError(syncErrorType));
            });
        }
        tooltip.showWithAnimation();
    }

    private void presentGenerateInfo() {
        tooltip.setInfo(R.string.tooltip_generate_info_message,
                v -> {
                    tooltipClickStream.onNext(ReportTooltipUiIndicator.generateInfo());
                    ((GenerateNavigator) this.getParentFragment()).navigateToGenerateTab();
                },
                v -> closeClickStream.onNext(ReportTooltipUiIndicator.generateInfo()));

        tooltip.showWithAnimation();
    }

    private void presentBackupReminder(int days) {
        tooltip.setInfoWithCloseIcon( days > 0 ? R.string.tooltip_backup_info_message : R.string.tooltip_no_backups_info_message,
                v -> {
                    tooltipClickStream.onNext(ReportTooltipUiIndicator.backupReminder(days));
                    ((BackupNavigator) this.getParentFragment()).navigateToBackup();
                },
                v -> closeClickStream.onNext(ReportTooltipUiIndicator.backupReminder(days)),
                days);

        tooltip.showWithAnimation();
    }

    private void presentImportInfo() {
        tooltip.setInfoMessage(R.string.tooltip_import_info_message);
        tooltip.showCancelButton(v -> closeClickStream.onNext(ReportTooltipUiIndicator.importInfo()));

        tooltip.showWithAnimation();
    }
}
