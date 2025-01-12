package com.wops.receiptsgo.widget.tooltip.report.backup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.provider.SyncProvider;
import com.wops.receiptsgo.widget.tooltip.report.backup.data.BackupReminderTooltipStorage;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BackupReminderTooltipManagerTest {

    private final static int RECEIPTS_FEW = 10;
    private final static int RECEIPTS_LOT = 20;
    private final static int DAYS_FEW = 7;
    private final static int DAYS_LOT = 12;
    private static final int NO_PREVIOUS_BACKUPS = -1;


    private BackupReminderTooltipManager manager;

    @Mock
    BackupProvidersManager backupProvidersManager;

    @Mock
    BackupReminderTooltipStorage storage;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        manager = new BackupReminderTooltipManager(backupProvidersManager, storage);

        when(backupProvidersManager.getSyncProvider()).thenReturn(SyncProvider.None);
    }

    @Test
    public void getEmptyIfAutoBackupsEnabled() {
        when(backupProvidersManager.getSyncProvider()).thenReturn(SyncProvider.GoogleDrive);

        final TestObserver<Integer> testObserver = manager.needToShowBackupReminder().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getEmptyIfFewReceiptsAndFewDays() {
        when(storage.getReceiptsCountWithoutBackup()).thenReturn(RECEIPTS_FEW);
        when(storage.getLastManualBackupDate()).thenReturn(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DAYS_FEW)));

        final TestObserver<Integer> testObserver = manager.needToShowBackupReminder().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertComplete()
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getEmptyIfFewNewReceipts() {
        when(storage.getReceiptsCountWithoutBackup()).thenReturn(RECEIPTS_FEW);
        when(storage.getLastManualBackupDate()).thenReturn(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DAYS_LOT)));

        final TestObserver<Integer> testObserver = manager.needToShowBackupReminder().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertComplete()
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getEmptyIfFewDays() {
        when(storage.getReceiptsCountWithoutBackup()).thenReturn(RECEIPTS_LOT);
        when(storage.getLastManualBackupDate()).thenReturn(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DAYS_FEW)));

        final TestObserver<Integer> testObserver = manager.needToShowBackupReminder().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertComplete()
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getDays() {
        when(storage.getReceiptsCountWithoutBackup()).thenReturn(RECEIPTS_LOT);
        when(storage.getLastManualBackupDate()).thenReturn(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DAYS_LOT)));

        final TestObserver<Integer> testObserver = manager.needToShowBackupReminder().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertComplete()
                .assertNoErrors()
                .assertValue(days -> days == DAYS_LOT);
    }

    @Test
    public void getEmptyBecauseOfProlongation() {
        when(storage.getReceiptsCountWithoutBackup()).thenReturn(RECEIPTS_LOT);
        when(storage.getLastManualBackupDate()).thenReturn(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DAYS_LOT)));
        when(storage.getProlongationsCount()).thenReturn(1);

        final TestObserver<Integer> testObserver = manager.needToShowBackupReminder().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertComplete()
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getDaysAfterProlongation() {
        when(storage.getReceiptsCountWithoutBackup()).thenReturn(RECEIPTS_LOT * 2);
        when(storage.getLastManualBackupDate()).thenReturn(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DAYS_LOT) * 2));
        when(storage.getProlongationsCount()).thenReturn(1);

        final TestObserver<Integer> testObserver = manager.needToShowBackupReminder().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertComplete()
                .assertNoErrors()
                .assertValue(days -> days == DAYS_LOT * 2);
    }

    @Test
    public void getMessageIfLotReceiptsAndNoDays() {
        when(storage.getReceiptsCountWithoutBackup()).thenReturn(RECEIPTS_LOT);
        when(storage.getLastManualBackupDate()).thenReturn(new Date(0));

        final TestObserver<Integer> testObserver = manager.needToShowBackupReminder().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertComplete()
                .assertNoErrors()
                .assertValue(days -> days == NO_PREVIOUS_BACKUPS);
    }
}
