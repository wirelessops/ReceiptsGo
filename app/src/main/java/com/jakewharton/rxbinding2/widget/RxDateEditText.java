package com.jakewharton.rxbinding2.widget;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.sql.Date;

import co.smartreceipts.android.date.DateEditText;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;


/**
 * Static factory methods for creating {@linkplain Observable observables} and {@linkplain Consumer
 * actions} for {@link RxDateEditText}.
 */
public class RxDateEditText {

    /**
     * Create an observable of character sequences for text changes on {@code view}.
     * <p>
     * <em>Warning:</em> Values emitted by this observable are <b>mutable</b> and owned by the host
     * {@code view} and thus are <b>not safe</b> to cache or delay reading (such as by observing
     * on a different thread).
     * <p>
     * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
     * to free this reference.
     * <p>
     * <em>Note:</em> A value will be emitted immediately on subscribe.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @CheckResult
    @NonNull
    public static Observable<Date> dateChanges(@NonNull DateEditText view) {
        Preconditions.checkNotNull(view, "view == null");
        return new TextViewTextObservable(view)
                .map(charSequence -> view.getDate());
    }
}
