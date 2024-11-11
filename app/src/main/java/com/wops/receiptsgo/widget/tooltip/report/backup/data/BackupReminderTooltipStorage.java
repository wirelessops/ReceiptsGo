package com.wops.receiptsgo.widget.tooltip.report.backup.data;

import java.sql.Date;

public interface BackupReminderTooltipStorage {

    void setLastManualBackupDate();

    Date getLastManualBackupDate();

    void setOneMoreNewReceipt();

    int getReceiptsCountWithoutBackup();

    void prolongReminder();

    int getProlongationsCount();
}
