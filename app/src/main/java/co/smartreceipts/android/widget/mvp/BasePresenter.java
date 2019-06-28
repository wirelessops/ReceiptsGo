package co.smartreceipts.android.widget.mvp;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Provides a base-level implementation of the {@link Presenter} contract, which automatically
 * handles the disposal of any active Rx streams to avoid memory leaks
 *
 * @param <ViewType> the View interface, which will be used to interact with the UI
 */
public abstract class BasePresenter<ViewType> implements Presenter<ViewType> {

    protected final ViewType view;
    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BasePresenter(@NonNull ViewType view) {
        this.view = Preconditions.checkNotNull(view);
    }

    @Override
    @CallSuper
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
