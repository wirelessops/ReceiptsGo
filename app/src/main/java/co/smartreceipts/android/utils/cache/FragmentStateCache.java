package co.smartreceipts.android.utils.cache;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.HashMap;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;

/**
 * Marking as deprecated while revisiting this as a solution for handling {@link android.os.TransactionTooLargeException}
 * when passing fragment arguments. The main caveat is that Android uses {@link Fragment#onCreate(Bundle)} by default to
 * recover from low memory situations, so we keep crashing when this was destroyed outside of the application scope and
 * Android attempts to re-create this
 */
@ApplicationScope
@Deprecated
public class FragmentStateCache {

    private final HashMap<Class<? extends Fragment>, Bundle> argumentsCacheHashMap = new HashMap<>();

    @Inject
    public FragmentStateCache() {
    }

    public void onDestroy(@NonNull Fragment fragment) {
        final FragmentActivity activity = fragment.getActivity();
        if (activity != null && !activity.isChangingConfigurations()) {
            if (activity.isDestroyed() && !activity.isFinishing()) {
                Logger.debug(this, "{}'s activity was released (but not destroyed) by Android. Retaining our cache", fragment.getClass());
            } else {
                Logger.debug(this, "Removing {} from the cache as it is being fully destroyed", fragment.getClass());
                remove(fragment.getClass());
            }
        }
    }

    private void remove(Class<? extends Fragment> fragmentClass) {
        argumentsCacheHashMap.remove(fragmentClass);
    }
}
