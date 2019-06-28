package co.smartreceipts.android.model.factory;

import androidx.annotation.NonNull;

/**
 * A simple builder factory interface that allows us {@link #build(From)} new instances of objects of
 * type {@link To} with an instance of {@link From}
 */
public interface BuilderFactory1<From, To> {

    /**
     * Creates a new instance of the desired factory object type
     *
     * @param from a {@link From} instance that can be used to build a {@link To}
     * @return a new instance of {@link To}
     */
    @NonNull
    To build(@NonNull From from);
}
