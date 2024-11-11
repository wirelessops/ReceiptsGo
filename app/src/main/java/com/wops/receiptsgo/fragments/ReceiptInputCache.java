package com.wops.receiptsgo.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.sql.Date;

import com.wops.receiptsgo.model.Category;

public class ReceiptInputCache {

    private final InputCacheHeadlessFragment mHeadlessFragment;

    public ReceiptInputCache(@NonNull FragmentManager fragmentManager) {
        InputCacheHeadlessFragment headlessFragment = (InputCacheHeadlessFragment) fragmentManager.findFragmentByTag(InputCacheHeadlessFragment.TAG);
        if (headlessFragment == null) {
            headlessFragment = new InputCacheHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, InputCacheHeadlessFragment.TAG).commit();
        }
        mHeadlessFragment = headlessFragment;
    }

    @Nullable
    public Date getCachedDate() {
        return mHeadlessFragment.mCachedDate;
    }

    public void setCachedDate(@Nullable Date cachedDate) {
        mHeadlessFragment.mCachedDate = cachedDate == null ? null : new Date(cachedDate.getTime() + 1);
    }

    @Nullable
    public Category getCachedCategory() {
        return mHeadlessFragment.mCachedCategory;
    }

    public void setCachedCategory(@Nullable Category cachedCategory) {
        mHeadlessFragment.mCachedCategory = cachedCategory;
    }

    public String getCachedCurrency() {
        return mHeadlessFragment.mCachedCurrency;
    }

    public void setCachedCurrency(@Nullable String cachedCurrency) {
        mHeadlessFragment.mCachedCurrency = cachedCurrency;
    }

    public static final class InputCacheHeadlessFragment extends Fragment {

        private static final String TAG = InputCacheHeadlessFragment.class.getName();

        private Date mCachedDate;
        private Category mCachedCategory;
        private String mCachedCurrency;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
