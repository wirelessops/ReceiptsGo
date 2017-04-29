package co.smartreceipts.android.permissions;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.analytics.Analytics;

public class HeadlessFragmentPermissionRequesterFactory {

    private final FragmentManager fragmentManager;
    private final Analytics analytics;

    public HeadlessFragmentPermissionRequesterFactory(@NonNull FragmentActivity activity, @NonNull Analytics analytics) {
        this.fragmentManager = Preconditions.checkNotNull(activity.getSupportFragmentManager());
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    @NonNull
    public PermissionRequesterHeadlessFragment get() {
        final String tag = PermissionRequesterHeadlessFragment.class.getName();
        PermissionRequesterHeadlessFragment fragment = (PermissionRequesterHeadlessFragment) this.fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new PermissionRequesterHeadlessFragment();
            fragment.analytics = this.analytics;
            this.fragmentManager.beginTransaction().add(fragment, tag).commitNow();
        }
        return fragment;
    }
}
