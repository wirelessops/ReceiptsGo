package co.smartreceipts.android.widget.tooltip.report.backup;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;

@ApplicationScope
public class BackupReminderTooltipManager {

    private final DatabaseHelper databaseHelper;
    private final BackupProvidersManager backupProvidersManager;

    @Inject
    public BackupReminderTooltipManager(DatabaseHelper databaseHelper, BackupProvidersManager backupProvidersManager) {
        this.databaseHelper = databaseHelper;
        this.backupProvidersManager = backupProvidersManager;
    }

    private void needToShowBackupReminderTooltip() {
        // TODO: 22.07.2017 count getLastDatabaseSyncTime and date if user dismissed the tooltip

        if (backupProvidersManager.getSyncProvider() == SyncProvider.None) { // auto-backups are disabled

        } else {

        }


        backupProvidersManager.getLastDatabaseSyncTime();

    }
}
