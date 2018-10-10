package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import io.reactivex.Single;

/**
 * Acts as a standard contract for the methods that all database tables should support, such as CRUD operations
 * and create/upgrade scripts
 *
 * @param <ModelType> the model object that this table will interact with
 */
public interface Table<ModelType extends Keyed> {

    /**
     * @return the table name for SQL operations
     */
    @NonNull
    String getTableName();

    /**
     * Called when this table is first created to allow us to build it out
     *
     * @param db         the {@link SQLiteDatabase} that database operations can be performed on
     * @param customizer a {@link TableDefaultsCustomizer} to allow us to insert the reasonable defaults
     */
    void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer);

    /**
     * Called whenever we upgrade our underlying database version
     *
     * @param db         the {@link SQLiteDatabase} that database operations can be performed on
     * @param oldVersion the old database version
     * @param newVersion the new database version
     * @param customizer a {@link TableDefaultsCustomizer} to allow us to insert the reasonable defaults
     */
    void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer);

    /**
     * Called after {@link #onCreate(SQLiteDatabase, TableDefaultsCustomizer)} and {@link #onUpgrade(SQLiteDatabase, int, int, TableDefaultsCustomizer)}
     * complete (if either was required) to allow us to perform clean-up operations
     */
    void onPostCreateUpgrade();

    /**
     * Retrieves list of all objects that are stored within this table. Please note that this is a blocking operation
     *
     * @return a {@link Single} with: a {@link List} of all objects of type {@link ModelType} that are stored within this table
     */
    @NonNull
    Single<List<ModelType>> get();

    /**
     * Attempts to look up an object based on it's primary key value for it's primary key column
     *
     * @param primaryKeyValue the primary key for this object
     * @return a {@link Single} with: the {@link ModelType} object or {@link Exception}
     */
    @NonNull
    Single<ModelType> findByPrimaryKey(int primaryKeyValue);

    /**
     * Inserts a new object of type {@link ModelType} into this table. Please note that this is a blocking operation
     *
     * @param modelType                 the object to insert
     * @param databaseOperationMetadata metadata about this particular database operation
     * @return a {@link Single} with: the inserted object of type {@link ModelType} or {@link Exception} if the insert failed
     */
    @NonNull
    Single<ModelType> insert(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    /**
     * Updates an existing object of type {@link ModelType} in this table. Please note that this is a blocking operation
     *
     * @param oldModelType              the old object that will be replaced
     * @param newModelType              the new object that will take the place of the old one
     * @param databaseOperationMetadata metadata about this particular database operation
     * @return a {@link Single} with: the updated object of type {@link ModelType} or {@link Exception} if the update failed
     */
    @NonNull
    Single<ModelType> update(@NonNull ModelType oldModelType, @NonNull ModelType newModelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    /**
     * Removes an existing object of type {@link ModelType} from this table. Please note that this is a blocking operation
     *
     * @param modelType                 the object to remove
     * @param databaseOperationMetadata metadata about this particular database operation
     * @return a {@link Single} with: the deleted {@link ModelType} if successful, {@link Exception} otherwise
     */
    @NonNull
    Single<ModelType> delete(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    /**
     * Deletes all row entries in this table
     */
    void deleteAllTableRowsBlocking();

    /**
     * Clears any cached data in our table
     */
    void clearCache();
}
