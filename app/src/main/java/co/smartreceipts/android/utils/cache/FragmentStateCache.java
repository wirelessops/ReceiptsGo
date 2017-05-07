package co.smartreceipts.android.utils.cache;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.HashMap;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;

@ApplicationScope
public class FragmentStateCache {

    private HashMap<Class<? extends Fragment>, Bundle> argumentsCacheHashMap = new HashMap<>();
    private HashMap<Class<? extends Fragment>, Bundle> stateCacheHashMap = new HashMap<>();

    @Inject
    public FragmentStateCache() {
    }

    public Bundle getArguments(Class<? extends Fragment> fragmentClass) {
        return argumentsCacheHashMap.get(fragmentClass);
    }

    public void putArguments(Bundle args, Class<? extends Fragment> fragmentClass) {
        argumentsCacheHashMap.put(fragmentClass, args);
    }

    public Bundle getSavedState(Class<? extends Fragment> fragmentClass) {
        return stateCacheHashMap.get(fragmentClass);
    }

    public void putSavedState(Bundle state, Class<? extends Fragment> fragmentClass) {
        stateCacheHashMap.put(fragmentClass, state);
    }

    public void onDestroy(Fragment fragment) {
        // clear cache if fragment is not going to be recreated
        if (fragment.getActivity() != null && !fragment.getActivity().isChangingConfigurations()) {
            remove(fragment.getClass());
        }
    }

    private void remove(Class<? extends Fragment> fragmentClass) {
        argumentsCacheHashMap.remove(fragmentClass);
        stateCacheHashMap.remove(fragmentClass);
    }
}
