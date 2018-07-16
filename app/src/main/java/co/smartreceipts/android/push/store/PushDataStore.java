package co.smartreceipts.android.push.store;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Lazy;
import io.reactivex.Single;

@ApplicationScope
public class PushDataStore {

    private static final String KEY_BOOL_REMOTE_REFRESH_REQUIRED = "key_bool_remote_refresh_required";

    private final Lazy<SharedPreferences> sharedPreferences;

    @Inject
    public PushDataStore(@NonNull Lazy<SharedPreferences> sharedPreferences) {
        this.sharedPreferences = Preconditions.checkNotNull(sharedPreferences);
    }

    /**
     * @return a {@link Single} that will emit {@code true} if a refresh is required and {@code false}
     * if one is not
     */
    @NonNull
    public Single<Boolean> isRemoteRefreshRequiredSingle() {
        return Single.fromCallable(this::isRemoteRefreshRequired);
    }

    public boolean isRemoteRefreshRequired() {
        return sharedPreferences.get().getBoolean(KEY_BOOL_REMOTE_REFRESH_REQUIRED, true);
    }

    public void setRemoteRefreshRequired(boolean refreshRequired) {
        sharedPreferences.get().edit().putBoolean(KEY_BOOL_REMOTE_REFRESH_REQUIRED, refreshRequired).apply();
    }
}
