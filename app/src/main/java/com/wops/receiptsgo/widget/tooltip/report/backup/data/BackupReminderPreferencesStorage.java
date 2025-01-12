package com.wops.receiptsgo.widget.tooltip.report.backup.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.sql.Date;

import javax.inject.Inject;

import com.wops.core.di.scopes.ApplicationScope;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.core.sync.provider.SyncProvider;
import com.wops.analytics.log.Logger;

@ApplicationScope
public class BackupReminderPreferencesStorage implements BackupReminderTooltipStorage {

    private final Context appContext;
    private final BackupProvidersManager backupProvidersManager;

    private final static class Keys {
        /**
         * Key to get preferences related to the backup reminder tooltip
         */
        private static final String BACKUP_TOOLTIP_PREFERENCES = "Backup Tooltip Preferences";
        /**
         * Key to track the date when the user created manual backup last time
         */
        private static final String USER_CREATED_MANUAL_BACKUP = "Last Manual Backup Date";
        /**
         * Key to track the count of new receipts that the user created after last manual backup
         */
        private static final String NEW_RECEIPTS_SINCE_LAST_MANUAL_BACKUP = "Receipts count since last Manual Backup";
        /**
         * Key to track how many times the user dismissed this reminder and did no manual backup
         */
        private static final String PROLONGATION_COUNTER = "Prolongation count";
    }

    @Inject
    public BackupReminderPreferencesStorage(Context appContext, BackupProvidersManager backupProvidersManager) {
        this.appContext = appContext.getApplicationContext();
        this.backupProvidersManager = backupProvidersManager;

        this.backupProvidersManager.registerChangeListener(newProvider -> {
            if (newProvider == SyncProvider.None) {
                clearTrackedValues();
            }
        });
    }

    @Override
    public void setLastManualBackupDate() {
        getSharedPreferences().edit()
                .putLong(Keys.USER_CREATED_MANUAL_BACKUP, System.currentTimeMillis())
                .putInt(Keys.NEW_RECEIPTS_SINCE_LAST_MANUAL_BACKUP, 0)
                .putInt(Keys.PROLONGATION_COUNTER, 0)
                .apply();
        Logger.debug(this, "Manual backup was created");
        Logger.debug(this, "Receipts without backup 0");

    }

    @Override
    public Date getLastManualBackupDate() {
        long millis = getSharedPreferences().getLong(Keys.USER_CREATED_MANUAL_BACKUP, 0);
        return new Date(millis);
    }

    @Override
    public void setOneMoreNewReceipt() {
        if (backupProvidersManager.getSyncProvider() == SyncProvider.None) {
            int receiptsCount = getReceiptsCountWithoutBackup();
            getSharedPreferences().edit()
                    .putInt(Keys.NEW_RECEIPTS_SINCE_LAST_MANUAL_BACKUP, receiptsCount + 1)
                    .apply();
            Logger.debug(this, "Receipts without backup " + (receiptsCount + 1));
        }
    }

    @Override
    public int getReceiptsCountWithoutBackup() {
        return getSharedPreferences().getInt(Keys.NEW_RECEIPTS_SINCE_LAST_MANUAL_BACKUP, 0);
    }

    @Override
    public void prolongReminder() {
        int prolongationCount = getProlongationsCount();

        getSharedPreferences().edit()
                .putInt(Keys.PROLONGATION_COUNTER, prolongationCount + 1)
                .apply();
    }

    @Override
    public int getProlongationsCount() {
        return getSharedPreferences().getInt(Keys.PROLONGATION_COUNTER, 0);
    }

    private SharedPreferences getSharedPreferences() {
        return appContext.getSharedPreferences(Keys.BACKUP_TOOLTIP_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void clearTrackedValues() {
        getSharedPreferences().edit()
                .putInt(Keys.NEW_RECEIPTS_SINCE_LAST_MANUAL_BACKUP, 0)
                .putLong(Keys.USER_CREATED_MANUAL_BACKUP, 0L)
                .putInt(Keys.PROLONGATION_COUNTER, 0)
                .apply();
    }


}
