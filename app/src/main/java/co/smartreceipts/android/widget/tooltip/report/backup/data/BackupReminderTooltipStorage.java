package co.smartreceipts.android.widget.tooltip.report.backup.data;

import java.sql.Date;

public interface BackupReminderTooltipStorage {

    boolean isAutomaticBackupsEnabled();

    Date getLastBackupDate();
}
