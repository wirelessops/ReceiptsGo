package co.smartreceipts.android.persistence.database.tables.adapters;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * A slightly extended version of the {@link DatabaseAdapter} contract that supports a specific selection type
 *
 * @param <ModelType> the model object type that this will be used to create
 * @param <SelectionModelType> the model type that was used in the 'WHERE' part of the 'SELECT' statement
 */
public interface SelectionBackedDatabaseAdapter<ModelType, SelectionModelType> extends DatabaseAdapter<ModelType> {

    /**
     * Consumes a database cursor in order to read a single entry for a given selection
     *
     * @param cursor the {@link Cursor} connection to the database
     * @param selectionModelType the {@link SelectionModelType} that was treated as the parent for this {@link ModelType}
     * @param isDescending {@code true} for descending order, {@code false} for ascending
     * @return an object of type {@link ModelType}, that is represented by the current row of the cursor
     */
    @NonNull
    ModelType readForSelection(@NonNull Cursor cursor, @NonNull SelectionModelType selectionModelType, boolean isDescending);

}
