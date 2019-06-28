package co.smartreceipts.android.persistence.database.controllers.alterations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.Table;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface TableActionAlterations<T> {

    /**
     * @return an {@link Completable} that should be performed before we call {@link Table#get()}
     */
    @NonNull
    Completable preGet();

    /**
     * Performs an action following a call to {@link Table#get()}
     *
     * @param list the list retrieved via the {@link Table#get()} request
     * @return a {@link Single} that has made any final  modifications
     */
    @NonNull
    Single<List<T>> postGet(@NonNull List<T> list);

    /**
     * @return a {@link Single} that should be performed before we call {@link Table#insert(T, DatabaseOperationMetadata)}}
     */
    @NonNull
    Single<T> preInsert(@NonNull T t);

    /**
     * Performs an action following a call to {@link Table#insert(T, DatabaseOperationMetadata)}
     *
     * @param t the item that was inserted in {@link Table#insert(T, DatabaseOperationMetadata)} or {@link Exception} if the insert failed
     * @return a {@link Single} that has made any final modifications
     */
    @NonNull
    Single<T> postInsert(@NonNull T t);

    /**
     * @return a {@link Single} that should be performed before we call {@link Table#update(T, T, DatabaseOperationMetadata)}
     */
    @NonNull
    Single<T> preUpdate(@NonNull T oldT, @NonNull T newT);

    /**
     * Performs an action following a call to {@link Table#update(T, T, DatabaseOperationMetadata)}
     *
     * @return a {@link Single} that has made any final  modifications
     */
    @NonNull
    Single<T> postUpdate(@NonNull T oldT, @Nullable T newT);

    /**
     * @return a {@link Single} that should be performed before we call {@link Table#delete(T, DatabaseOperationMetadata)}
     */
    @NonNull
    Single<T> preDelete(@NonNull T t);

    /**
     * Performs an action following a call to {@link Table#delete(T, DatabaseOperationMetadata)}
     *
     * @param t the item that was inserted in {@link Table#delete(T, DatabaseOperationMetadata)} or {@code null} if the delete failed
     * @return a {@link Single} that has made any final modifications
     */
    Single<T> postDelete(@Nullable T t);
}
