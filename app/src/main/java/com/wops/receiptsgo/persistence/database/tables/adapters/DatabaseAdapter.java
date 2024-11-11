package com.wops.receiptsgo.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import java.util.UUID;

import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;

/**
 * Enables to quickly convert a model object to/from Android database objects
 *
 * @param <ModelType> the model object type that this will be used to create
 */
public interface DatabaseAdapter<ModelType> {

    /**
     * Consumes a database cursor in order to read a single entry
     *
     * @param cursor the {@link Cursor} connection to the database
     * @return an object of type {@link ModelType}, that is represented by the current row of the cursor
     */
    @NonNull
    ModelType read(@NonNull Cursor cursor);

    /**
     * Takes a single object of type {@link ModelType} and converts it to a set of {@link ContentValues}
     *
     * @param modelType                 the object to write to a set of {@link ContentValues}
     * @param databaseOperationMetadata metadata about this particular database operation
     * @return the set of {@link ContentValues}
     */
    @NonNull
    ContentValues write(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    /**
     * When an auto-increment primary key is used, we may need to generate an entry with this new id
     * (as based off the underlying values of the original object)
     *
     * @param modelType                 the object to serve as the "base"
     * @param primaryKey                the primary key int value
     * @param uuid                      the unique identifier
     * @param databaseOperationMetadata metadata about this particular database operation
     * @return the object param or a new object of type {@link ModelType} if this primary key is needed
     */
    @NonNull
    ModelType build(@NonNull ModelType modelType, int primaryKey, @NonNull UUID uuid, @NonNull DatabaseOperationMetadata databaseOperationMetadata);
}
