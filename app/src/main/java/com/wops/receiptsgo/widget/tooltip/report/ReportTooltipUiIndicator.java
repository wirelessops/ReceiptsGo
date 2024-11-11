package com.wops.receiptsgo.widget.tooltip.report;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import co.smartreceipts.core.sync.errors.SyncErrorType;

public class ReportTooltipUiIndicator {

    public enum State {
        SyncError, GenerateInfo, BackupReminder, ImportInfo, None
    }

    private final State state;
    private final Optional<SyncErrorType> errorType;
    private final Optional<Integer> daysSinceBackup;

    private ReportTooltipUiIndicator(@NonNull State state, @Nullable SyncErrorType errorType,
                                     @Nullable Integer daysSinceLastBackup) {
        this.state = Preconditions.checkNotNull(state);
        this.errorType = Optional.ofNullable(errorType);
        this.daysSinceBackup = Optional.ofNullable(daysSinceLastBackup);
    }

    @NonNull
    public static ReportTooltipUiIndicator syncError(@NonNull SyncErrorType errorType) {
        return new ReportTooltipUiIndicator(State.SyncError, errorType, null);
    }

    @NonNull
    public static ReportTooltipUiIndicator generateInfo() {
        return new ReportTooltipUiIndicator(State.GenerateInfo, null, null);
    }

    @NonNull
    public static ReportTooltipUiIndicator backupReminder(int days) {
        return new ReportTooltipUiIndicator(State.BackupReminder, null, days);
    }

    @NonNull
    public static ReportTooltipUiIndicator importInfo() {
        return new ReportTooltipUiIndicator(State.ImportInfo, null, null);
    }

    @NonNull
    public static ReportTooltipUiIndicator none() {
        return new ReportTooltipUiIndicator(State.None, null, null);
    }

    public State getState() {
        return state;
    }

    public Optional<SyncErrorType> getErrorType() {
        return errorType;
    }

    public Optional<Integer> getDaysSinceBackup() {
        return daysSinceBackup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportTooltipUiIndicator indicator = (ReportTooltipUiIndicator) o;

        if (state != indicator.state) return false;
        if (!errorType.equals(indicator.errorType)) return false;
        return daysSinceBackup.equals(indicator.daysSinceBackup);

    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = 31 * result + errorType.hashCode();
        result = 31 * result + daysSinceBackup.hashCode();
        return result;
    }
}
