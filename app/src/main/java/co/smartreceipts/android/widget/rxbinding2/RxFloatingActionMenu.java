package co.smartreceipts.android.widget.rxbinding2;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import com.github.clans.fab.FloatingActionMenu;
import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding2.InitialValueObservable;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;


/**
 * Static factory methods for creating {@linkplain Observable observables} and {@linkplain Consumer
 * actions} for {@link FloatingActionMenu}.
 */
public class RxFloatingActionMenu {

    /**
     * Create an observable of booleans representing the toggled state of {@code view}.
     * <p>
     * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Dispose
     * to free this reference.
     * <p>
     * <em>Warning:</em> The created observable uses {@link FloatingActionMenu#setOnMenuToggleListener(FloatingActionMenu.OnMenuToggleListener)}
     * to observe toggle changes. Only one observable can be used for a view at a time.
     * <p>
     * <em>Note:</em> A value will be emitted immediately on subscribe.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @CheckResult
    @NonNull
    public static InitialValueObservable<Boolean> toggleChanges(@NonNull FloatingActionMenu view) {
        Preconditions.checkNotNull(view, "view == null");
        return new FloatingActionMenuToggledObservable(view);
    }
}
