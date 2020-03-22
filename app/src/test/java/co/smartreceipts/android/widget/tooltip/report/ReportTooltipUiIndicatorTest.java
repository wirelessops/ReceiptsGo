package co.smartreceipts.android.widget.tooltip.report;

import org.junit.Test;

import co.smartreceipts.core.sync.errors.SyncErrorType;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipUiIndicator.State;

import static junit.framework.Assert.assertEquals;

public class ReportTooltipUiIndicatorTest {

    private static final int DAYS = 5;

    @Test
    public void error() {
        final ReportTooltipUiIndicator indicator = ReportTooltipUiIndicator.syncError(SyncErrorType.NoRemoteDiskSpace);

        assertEquals(ReportTooltipUiIndicator.syncError(SyncErrorType.NoRemoteDiskSpace), indicator);
        assertEquals(State.SyncError, indicator.getState());
        assertEquals(SyncErrorType.NoRemoteDiskSpace, indicator.getErrorType().get());
    }

    @Test
    public void generateInfo() {
        final ReportTooltipUiIndicator indicator = ReportTooltipUiIndicator.generateInfo();

        assertEquals(ReportTooltipUiIndicator.generateInfo(), indicator);
        assertEquals(State.GenerateInfo, indicator.getState());
        assertEquals(null, indicator.getErrorType().orNull());
    }

    @Test
    public void backupReminder() {
        final ReportTooltipUiIndicator indicator = ReportTooltipUiIndicator.backupReminder(DAYS);

        assertEquals(ReportTooltipUiIndicator.backupReminder(DAYS), indicator);
        assertEquals(State.BackupReminder, indicator.getState());
        assertEquals(DAYS, (int)indicator.getDaysSinceBackup().get());
    }

    @Test
    public void importInfo() {
        final ReportTooltipUiIndicator indicator = ReportTooltipUiIndicator.importInfo();

        assertEquals(ReportTooltipUiIndicator.importInfo(), indicator);
        assertEquals(State.ImportInfo, indicator.getState());
        assertEquals(null, indicator.getErrorType().orNull());
    }
}
