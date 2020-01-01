package co.smartreceipts.android.widget.viper;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.widget.mvp.BasePresenter;

/**
 * Augments the {@link BasePresenter} by defining an {@link InteractorType}, which is required any
 * time we require actions that can be performed in our {@link ApplicationScope}. This aligns with
 * the more general "VIPER" architecture pattern.
 *
 * @param <ViewType> the View interface, which will be used to interact with the UI
 * @param <InteractorType> the Interactor interface, which will be used to perform tasks that may
 * need to exist at the in our {@link ApplicationScope} (ie survive rotations)
 */
public abstract class BaseViperPresenter<ViewType, InteractorType> extends BasePresenter<ViewType> {

    protected final InteractorType interactor;

    public BaseViperPresenter(@NonNull ViewType view, @NonNull InteractorType interactor) {
        super(view);
        this.interactor = Preconditions.checkNotNull(interactor);
    }

}
