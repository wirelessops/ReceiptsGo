package com.wops.receiptsgo.fragments;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.wops.receiptsgo.ReceiptsGoApplication;

import wb.android.flex.Flex;

public class WBListFragment extends ListFragment {

	private ReceiptsGoApplication mApplication;
//	private DateManager mDateManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = getSmartReceiptsApplication();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mApplication = null;
	}

	protected String getFlexString(Flex flex, int id) {
		if (isAdded()) {
			return flex.getString(getActivity(), id);
		}
		else {
			return "";
		}
	}

    public final void setSupportActionBar(@Nullable Toolbar toolbar) {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
    }

    @Nullable
    public final ActionBar getSupportActionBar() {
		final Activity activity = getActivity();
		if (activity instanceof AppCompatActivity) {
            return ((AppCompatActivity)activity).getSupportActionBar();
        }
        return null;
    }

	public ReceiptsGoApplication getSmartReceiptsApplication() {
		if (mApplication == null) {
			if (getActivity() != null) {
				final Application application = getActivity().getApplication();
				if (application instanceof ReceiptsGoApplication) {
					mApplication = (ReceiptsGoApplication) application;
				}
				else {
					throw new RuntimeException("The Application must be an instance a SmartReceiptsApplication");
				}
			}
		}
		return mApplication;
	}

}