package com.wops.receiptsgo.persistence.database.controllers.alterations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * A stub implementation of the {@link TableActionAlterations} contract that allows us to selectively override methods (if needed)
 *
 * @param <T> the type of object we will be interacting with
 */
public class StubTableActionAlterations<T> implements TableActionAlterations<T> {

    @NonNull
    @Override
    public Completable preGet() {
        return Completable.complete();
    }

    @NonNull
    @Override
    public Single<List<T>> postGet(@NonNull List<T> list) {
        return Single.just(list);
    }

    @NonNull
    @Override
    public Single<T> preInsert(@NonNull T t) {
        return Single.just(t);
    }

    @NonNull
    @Override
    public Single<T> postInsert(@NonNull T t) {
        return Single.fromCallable(() -> {
            if (t == null) {
                throw new Exception("Post insert failed due to a null value");
            }
            return t;
        });
    }

    @NonNull
    @Override
    public Single<T> preUpdate(@NonNull T oldT, @NonNull T newT) {
        return Single.just(newT);
    }

    @NonNull
    @Override
    public Single<T> postUpdate(@NonNull T oldT, @Nullable T newT) {
        return Single.fromCallable(() -> {
            if (newT == null) {
                throw new Exception("Post update failed due to a null value");
            }

            return newT;
        });
    }

    @NonNull
    @Override
    public Single<T> preDelete(@NonNull T t) {
        return Single.just(t);
    }

    @NonNull
    @Override
    public Single<T> postDelete(@Nullable T t) {
        return Single.fromCallable(() -> {
            if (t == null) {
                throw new Exception("Post delete failed due to a null value");
            }

            return t;
        });
    }
}
