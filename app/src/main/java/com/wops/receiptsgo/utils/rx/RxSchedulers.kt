package com.wops.receiptsgo.utils.rx

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Provides a series of constants that we can use in conjunction with Dagger to inject the desired
 * [Schedulers] for dependency injection
 */
object RxSchedulers {
    /**
     * A constant value that should map to [Schedulers.io]
     */
    const val IO = "io"
    /**
     * A constant value that should map to [Schedulers.computation]
     */
    const val COMPUTATION = "computation"
    /**
     * A constant value that should map to [AndroidSchedulers.mainThread]
     */
    const val MAIN = "main"
}