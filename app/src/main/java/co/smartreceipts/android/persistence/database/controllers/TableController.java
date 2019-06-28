package co.smartreceipts.android.persistence.database.controllers;

import androidx.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import org.reactivestreams.Subscriber;

import java.util.List;

import co.smartreceipts.android.persistence.database.controllers.results.DeleteResult;
import co.smartreceipts.android.persistence.database.controllers.results.GetResult;
import co.smartreceipts.android.persistence.database.controllers.results.InsertResult;
import co.smartreceipts.android.persistence.database.controllers.results.UpdateResult;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Provides an asynchronous way for us to easily interact with our database layer
 *
 * @param <ModelType> the model object type that this will be used to create
 */
public interface TableController<ModelType> {

    /**
     * Registers a listener that will receive appropriate callbacks based on table events
     */
    void subscribe(@NonNull TableEventsListener<ModelType> tableEventsListener);

    /**
     * Removes a listener that was previously registered, so it will no longer receive events
     */
    void unsubscribe(@NonNull TableEventsListener<ModelType> tableEventsListener);

    /**
     * Retrieves list of all objects that are stored within this table.
     */
    @NonNull
    Single<List<ModelType>> get();

    /**
     * Returns a stream of all get requests submitted to {@link #get()}
     * <p>
     * Please note that this will never call {@link Subscriber#onError(Throwable)} or {@link Subscriber#onComplete()},
     * since we want to ensure that this stream never ends in order to allow listeners to observe this for the app lifetime
     * </p>
     * @return an {@link Observable} that will emit all {@link GetResult}s of this {@link ModelType}
     */
    @NonNull
    Observable<GetResult<ModelType>> getStream();

    /**
     * Inserts a new object of type {@link ModelType} into this table.
     *
     * @param modelType the object to insert
     * @param databaseOperationMetadata metadata about this particular database operation
     * @return a hot {@link Observable} that will return the result of this operation
     */
    Observable<Optional<ModelType>> insert(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    /**
     * Returns a stream of all insertions submitted to {@link #insert(Object, DatabaseOperationMetadata)}
     * <p>
     * Please note that this will never call {@link Subscriber#onError(Throwable)} or {@link Subscriber#onComplete()},
     * since we want to ensure that this stream never ends in order to allow listeners to observe this for the app lifetime
     * </p>
     * @return an {@link Observable} that will emit all {@link InsertResult}s of this {@link ModelType}
     */
    @NonNull
    Observable<InsertResult<ModelType>> insertStream();

    /**
     * Updates an existing object of type {@link ModelType} in this table.
     *
     * @param oldModelType the old object that will be replaced
     * @param newModelType the new object that will take the place of the old one
     * @param databaseOperationMetadata metadata about this particular database operation
     * @return a hot {@link Observable} that will return the updated {@link ModelType} as the result of this operation
     */
    Observable<Optional<ModelType>> update(@NonNull ModelType oldModelType, @NonNull ModelType newModelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    /**
     * Returns a stream of all updates submitted to {@link #update(Object, Object, DatabaseOperationMetadata)}
     * <p>
     * Please note that this will never call {@link Subscriber#onError(Throwable)} or {@link Subscriber#onComplete()},
     * since we want to ensure that this stream never ends in order to allow listeners to observe this for the app lifetime
     * </p>
     * @return an {@link Observable} that will emit all {@link UpdateResult}s of this {@link ModelType}
     */
    @NonNull
    Observable<UpdateResult<ModelType>> updateStream();

    /**
     * Removes an existing object of type {@link ModelType} from this table.
     *
     * @param modelType the object to remove
     * @param databaseOperationMetadata metadata about this particular database operation
//     * @return a hot {@link Observable} that will return the result of this operation
     */
    /*Observable<ModelType>*/ void delete(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    /**
     * Returns a stream of all deletions submitted to {@link #delete(Object, DatabaseOperationMetadata)}
     * <p>
     * Please note that this will never call {@link Subscriber#onError(Throwable)} or {@link Subscriber#onComplete()},
     * since we want to ensure that this stream never ends in order to allow listeners to observe this for the app lifetime
     * </p>
     * @return an {@link Observable} that will emit all {@link DeleteResult}s of this {@link ModelType}
     */
    @NonNull
    Observable<DeleteResult<ModelType>> deleteStream();
}
