package co.smartreceipts.android.persistence.database.restore;

import android.support.annotation.NonNull;

import co.smartreceipts.android.persistence.DatabaseHelper;
import io.reactivex.Completable;

/**
 * Defines a contract in which two databases can be merged
 */
interface DatabaseMerger {

    /**
     * Initiates a process in which two databases can be merged
     *
     * @param currentDatabase the current {@link DatabaseHelper}
     * @param importedBackupDatabase the imported {@link DatabaseHelper}, where all tables have been
     * prefixed by "backup_db."
     *
     * @return a {@link Completable}, which will emit a success/error event depending on the result
     * of the merge process
     */
    @NonNull
    Completable merge(@NonNull DatabaseHelper currentDatabase, @NonNull DatabaseHelper importedBackupDatabase);
}
