package co.smartreceipts.android.utils.butterknife;

import androidx.annotation.NonNull;
import android.view.View;

import butterknife.Action;

public class ButterKnifeActions {

    @NonNull
    public static Action<View> setEnabled(final boolean isEnabled) {
        return (view, index) -> view.setEnabled(isEnabled);
    }

    @NonNull
    public static Action<View> setVisibility(int visibility) {
        return (view, index) -> view.setVisibility(visibility);
    }
}
