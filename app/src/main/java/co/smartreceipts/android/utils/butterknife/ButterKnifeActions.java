package co.smartreceipts.android.utils.butterknife;

import android.support.annotation.NonNull;
import android.view.View;

import butterknife.ButterKnife;

public class ButterKnifeActions {

    @NonNull
    public static ButterKnife.Action<View> setEnabled(final boolean isEnabled) {
        return (view, index) -> view.setEnabled(isEnabled);
    }

    @NonNull
    public static ButterKnife.Action<View> setVisibility(int visibility) {
        return (view, index) -> view.setVisibility(visibility);
    }
}
