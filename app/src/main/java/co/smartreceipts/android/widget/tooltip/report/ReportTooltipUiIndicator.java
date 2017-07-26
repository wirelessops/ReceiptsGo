package co.smartreceipts.android.widget.tooltip.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import co.smartreceipts.android.sync.errors.SyncErrorType;

public class ReportTooltipUiIndicator {

    public enum State {
        SyncError, GenerateInfo, BackupReminder, None
    }

    private final State state;
    private final Optional<SyncErrorType> errorType;

    private ReportTooltipUiIndicator(@NonNull State state, @Nullable SyncErrorType errorType) {
        this.state = Preconditions.checkNotNull(state);
        this.errorType = Optional.ofNullable(errorType);
    }

    @NonNull
    public static ReportTooltipUiIndicator syncError(@NonNull SyncErrorType errorType) {
        return new ReportTooltipUiIndicator(State.SyncError, errorType);
    }

    @NonNull
    public static ReportTooltipUiIndicator generateInfo() {
        return new ReportTooltipUiIndicator(State.GenerateInfo, null);
    }

    @NonNull
    public static ReportTooltipUiIndicator backupReminder() {
        return new ReportTooltipUiIndicator(State.BackupReminder, null);
    }

    @NonNull
    public static ReportTooltipUiIndicator none() {
        return new ReportTooltipUiIndicator(State.None, null);
    }

    public State getState() {
        return state;
    }

    public Optional<SyncErrorType> getErrorType() {
        return errorType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportTooltipUiIndicator that = (ReportTooltipUiIndicator) o;

        if (state != that.state) return false;
        return errorType.equals(that.errorType);

    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = 31 * result + errorType.hashCode();
        return result;
    }
}
