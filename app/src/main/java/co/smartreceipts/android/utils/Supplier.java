package co.smartreceipts.android.utils;

import android.support.annotation.NonNull;

/**
 * A simple interface to define a factory-type pattern in which a certain value can be supplied
 *
 * @param <Type> the class type to provide
 */
public interface Supplier<Type> {

    /**
     * @return a non-null instance of type {@link Type}
     */
    @NonNull
    Type get();
}
