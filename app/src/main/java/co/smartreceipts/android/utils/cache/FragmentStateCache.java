package co.smartreceipts.android.utils.cache;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.HashMap;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;

@ApplicationScope
public class FragmentStateCache {

    private final HashMap<Class<? extends Fragment>, Bundle> argumentsCacheHashMap = new HashMap<>();
    private final HashMap<Class<? extends Fragment>, Bundle> stateCacheHashMap = new HashMap<>();

    @Inject
    public FragmentStateCache() {
    }

    public Bundle getArguments(@NonNull Class<? extends Fragment> fragmentClass) {
        return argumentsCacheHashMap.get(fragmentClass);
    }

    public void putArguments(@NonNull Bundle args, @NonNull Class<? extends Fragment> fragmentClass) {
        argumentsCacheHashMap.put(fragmentClass, args);
    }

    public Bundle getSavedState(@NonNull Class<? extends Fragment> fragmentClass) {
        return stateCacheHashMap.get(fragmentClass);
    }

    public void putSavedState(@NonNull Bundle state, @NonNull Class<? extends Fragment> fragmentClass) {
        stateCacheHashMap.put(fragmentClass, state);
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
        stateCacheHashMap.remove(fragmentClass);
    }
}
