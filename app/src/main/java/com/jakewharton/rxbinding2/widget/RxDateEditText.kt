package com.jakewharton.rxbinding2.widget

import androidx.annotation.CheckResult
import com.wops.receiptsgo.date.DateEditText
import com.google.common.base.Preconditions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import java.sql.Date


/**
 * Static factory methods for creating [observables][Observable] and [ actions][Consumer] for [RxDateEditText].
 */
object RxDateEditText {

    /**
     * Create an observable of character sequences for text changes on `view`.
     *
     *
     * *Warning:* Values emitted by this observable are **mutable** and owned by the host
     * `view` and thus are **not safe** to cache or delay reading (such as by observing
     * on a different thread).
     *
     *
     * *Warning:* The created observable keeps a strong reference to `view`. Unsubscribe
     * to free this reference.
     *
     *
     * *Note:* A value will be emitted immediately on subscribe.
     */
    @CheckResult
    fun dateChanges(view: DateEditText): Observable<Date> {
        Preconditions.checkNotNull(view, "view == null")
        return view.textChanges()
            .map { view.date }
    }
}
