package co.smartreceipts.android.widget.rxbinding2;

import com.github.clans.fab.FloatingActionMenu;
import com.jakewharton.rxbinding3.InitialValueObservable;

import co.smartreceipts.android.widget.ThreadPreconditions;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Adopts the same patters introduced via the {@link com.jakewharton.rxbinding3.widget.RxCompoundButton}
 * library in a manner that is viable for the third-party {@link FloatingActionMenu} button.
 */
class FloatingActionMenuToggledObservable extends InitialValueObservable<Boolean> {
    private final FloatingActionMenu view;

    FloatingActionMenuToggledObservable(FloatingActionMenu view) {
        this.view = view;
    }

    @Override protected void subscribeListener(Observer<? super Boolean> observer) {
        if (!ThreadPreconditions.checkMainThread(observer)) {
            return;
        }
        FloatingActionMenuToggledObservable.Listener listener = new FloatingActionMenuToggledObservable.Listener(view, observer);
        observer.onSubscribe(listener);
        view.setOnMenuToggleListener(listener);
    }

    @Override
    protected Boolean getInitialValue() {
        return view.isOpened();
    }

    static final class Listener extends MainThreadDisposable implements FloatingActionMenu.OnMenuToggleListener {
        private final FloatingActionMenu view;
        private final Observer<? super Boolean> observer;

        Listener(FloatingActionMenu view, Observer<? super Boolean> observer) {
            this.view = view;
            this.observer = observer;
        }

        @Override
        public void onMenuToggle(boolean opened) {
            if (!isDisposed()) {
                observer.onNext(opened);
            }
        }

        @Override
        protected void onDispose() {
            view.setOnMenuToggleListener(null);
        }
    }
}
