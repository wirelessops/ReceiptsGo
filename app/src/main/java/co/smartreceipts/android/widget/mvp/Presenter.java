package co.smartreceipts.android.widget.mvp;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Used to standardize our core presentation layer to ensure that we're using common patterns for
 * subscribing and un-subscribing to event streams
 * <p>
 * As this contract is usually tied to a specific {@link ViewType}, we will almost always wish to tie
 * this to a {@link Fragment} or {@link android.app.Activity} scope (as opposed to the application
 * one)
 * </p>
 *
 * @param <ViewType> the View interface, which will be used to interact with the UI
 */
public interface Presenter<ViewType> {

    /**
     * Informs the presenter that we should subscribe to begin listening to various event streams
     * <p>
     * Generally speaking, this should be called immediately after one of the super calls to
     * {@link Fragment} or {@link android.app.Activity} "creation" events (eg
     * {@link Fragment#onCreate(Bundle)}, {@link Fragment#onStart()}, or {@link Fragment#onResume()})
     * </p>
     */
    void subscribe();

    /**
     * Informs the presenter that we should un-subscribe to stop listening to various event streams
     * <p>
     * Generally speaking, this should be called immediately before one of the super calls to one of
     * the {@link Fragment} or {@link android.app.Activity} "destruction" events (eg
     * {@link Fragment#onDestroy()}, {@link Fragment#onStop()}, or {@link Fragment#onPause()} 
     * </p>
     */
    void unsubscribe();

}
