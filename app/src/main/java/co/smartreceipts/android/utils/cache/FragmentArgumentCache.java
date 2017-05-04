package co.smartreceipts.android.utils.cache;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;

@ApplicationScope
public class FragmentArgumentCache {

    // TODO: 04.05.2017 research about onSaveInstanceState

    private ConcurrentHashMap<Class<? extends Fragment>, Bundle> cacheHashMap = new ConcurrentHashMap<>();

    @Inject
    public FragmentArgumentCache() {
    }

    // Returns a bundle of fragment arguments
    public Bundle get(Class<? extends Fragment> fragmentClass) {
        return cacheHashMap.get(fragmentClass);
    }

    // Puts a set of args for a particular fragment class key
    public void put(Bundle args, Class<? extends Fragment> fragmentClass) {
        cacheHashMap.put(fragmentClass, args);
        Logger.debug(this, "after put Cache =" + cacheHashMap.toString());
    }

    public void remove(Class<? extends Fragment> fragment) {
        cacheHashMap.remove(fragment);
        Logger.debug(this, "after remove Cache =" + cacheHashMap.toString());
    }

    public void clear() {
        cacheHashMap.clear();
    }
}
