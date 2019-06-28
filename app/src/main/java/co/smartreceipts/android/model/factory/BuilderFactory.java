package co.smartreceipts.android.model.factory;

import androidx.annotation.NonNull;

/**
 * A simple builder factory interface that allows us {@link #build()} new instances of objects of
 * type {@link T}
 */
public interface BuilderFactory<T> {

    /**
     * Creates a new instance of the desired factory object type
     *
     * @return a new instance of {@link T}
     */
    @NonNull
    T build();
}
