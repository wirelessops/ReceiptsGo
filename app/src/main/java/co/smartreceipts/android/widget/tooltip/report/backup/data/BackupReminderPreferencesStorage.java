package co.smartreceipts.android.widget.tooltip.report.backup.data;

import java.sql.Date;

public class BackupReminderPreferencesStorage implements BackupReminderTooltipStorage {

    @Override
    public boolean isAutomaticBackupsEnabled() {
        return false;
    }

    @Override
    public Date getLastBackupDate() {
        return null;
    }
}
