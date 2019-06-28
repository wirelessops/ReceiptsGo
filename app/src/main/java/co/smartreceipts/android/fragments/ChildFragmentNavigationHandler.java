package co.smartreceipts.android.fragments;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.common.base.Preconditions;

public class ChildFragmentNavigationHandler {

    private final FragmentManager fragmentManager;

    public ChildFragmentNavigationHandler(@NonNull Fragment fragment) {
        this.fragmentManager = Preconditions.checkNotNull(fragment.getChildFragmentManager());
    }

    public void addChild(@NonNull Fragment childFragment, @IdRes int toViewRes) {
        Preconditions.checkNotNull(childFragment);
        this.fragmentManager.beginTransaction().replace(toViewRes, childFragment).commit();
    }
}
