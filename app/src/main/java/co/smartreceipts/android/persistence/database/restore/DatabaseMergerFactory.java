package co.smartreceipts.android.persistence.database.restore;

import androidx.annotation.NonNull;

/**
 * A factory, which will return an appropriate version of the {@link DatabaseMerger} based on if we
 * are overwriting our data or not.
 */
class DatabaseMergerFactory {

    /**
     * Gets an appropriate version of the {@link DatabaseMerger}
     *
     * @param overwriteExistingData whether we overwrite the existing data or not
     * @return an appropriate version of the {@link DatabaseMerger}
     */
    @NonNull
    public DatabaseMerger get(boolean overwriteExistingData) {
        if (overwriteExistingData) {
            return new OverwriteDatabaseMerger();
        } else {
            return new ByRowDatabaseMerger();
        }
    }
}
