package com.wops.receiptsgo.utils.rx;

import com.google.common.base.Preconditions;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.ErrorEvent;
import com.wops.analytics.log.Logger;
import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;

/**
 * Provides a default implementation of the {@link Consumer} protocol in order to allow us to handle
 * the various RxJava exceptions that are manifested in
 * {@link io.reactivex.plugins.RxJavaPlugins#setErrorHandler(Consumer)}. We use this to avoid
 * crashing the application in all scenarios that an exception was not handled.
 *
 * @see <a href="https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling">ErrorHandling</a>
 */
public class DefaultRxErrorHandler implements Consumer<Throwable> {

    private final Analytics analytics;

    public DefaultRxErrorHandler(@NonNull Analytics analytics) {
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    @Override
    public void accept(@NonNull Throwable throwable) throws Exception {
        analytics.record(new ErrorEvent(throwable));
        if (throwable instanceof UndeliverableException) {
            final UndeliverableException undeliverableException = (UndeliverableException) throwable;
            final Throwable cause = undeliverableException.getCause();
            if (cause instanceof RuntimeException) {
                uncaught(cause);
            } else {
                Logger.warn(DefaultRxErrorHandler.this, "Suppressing checked exception that was not delivered.", cause);
            }
        } else {
            uncaught(throwable);
        }
    }

    /**
     * Crashes the app
     *
     * @param error the {@link Throwable} exception that was not caught
     */
    private static void uncaught(@NonNull Throwable error) {
        final Thread currentThread = Thread.currentThread();
        Thread.UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
        handler.uncaughtException(currentThread, error);
    }
}
