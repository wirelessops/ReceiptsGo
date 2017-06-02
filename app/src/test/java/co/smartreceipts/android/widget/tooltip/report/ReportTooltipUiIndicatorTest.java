package co.smartreceipts.android.widget.tooltip.report;

import org.junit.Test;

import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipUiIndicator.State;

import static junit.framework.Assert.assertEquals;

public class ReportTooltipUiIndicatorTest {

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
}
