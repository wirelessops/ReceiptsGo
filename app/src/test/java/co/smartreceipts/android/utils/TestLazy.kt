package co.smartreceipts.android.utils

import dagger.Lazy
import dagger.internal.DoubleCheck
import dagger.internal.Factory
import dagger.internal.InstanceFactory

/**
 * A simple wrapper around Dagger2 [DoubleCheck] and [InstanceFactory] code, which is
 * designed to allow us to easily create [Lazy] instances for our testing purposes
 */
object TestLazy {

    /**
     * Create a [Lazy] provider of [T]
     *
     * @param t the item to be lazily returned
     * @param <T> the parameterized type, [T]
     * @return a [Lazy] provider of [T]
     */
    @JvmStatic
    fun <T> create(t: T): Lazy<T> {
        return DoubleCheck.lazy<Factory<T>, T>(InstanceFactory.create<T>(t))
    }
}
