package co.smartreceipts.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.google.common.base.Preconditions;

import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.TripFragmentPagerAdapter;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.LastTripController;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.sync.widget.errors.SyncErrorFragment;
import co.smartreceipts.android.utils.cache.FragmentStateCache;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.support.AndroidSupportInjection;

public class ReportInfoFragment extends WBFragment {

    public static final String TAG = ReportInfoFragment.class.getSimpleName();

    private static final String KEY_OUT_TRIP = "key_out_trip";

    @Inject
    ConfigurationManager configurationManager;
    @Inject
    TripTableController tripTableController;
    @Inject
    NavigationHandler navigationHandler;
    @Inject
    FragmentStateCache fragmentStateCache;

    private LastTripController mLastTripController;
    private TripFragmentPagerAdapter mFragmentPagerAdapter;
    private Trip mTrip;
    private ActionBarTitleUpdatesListener mActionBarTitleUpdatesListener;

    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;

    @NonNull
    public static ReportInfoFragment newInstance() {
        return new ReportInfoFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");
        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            mTrip = fragmentStateCache.getArguments(getClass()).getParcelable(Trip.PARCEL_KEY);
        } else {
            mTrip = fragmentStateCache.getSavedState(getClass()).getParcelable(KEY_OUT_TRIP);
        }
        Preconditions.checkNotNull(mTrip, "A valid trip is required");
        mLastTripController = new LastTripController(getActivity());
        mFragmentPagerAdapter = new TripFragmentPagerAdapter(getResources(), getChildFragmentManager(),
                configurationManager);
        mActionBarTitleUpdatesListener = new ActionBarTitleUpdatesListener();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.report_info_view_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            new ChildFragmentNavigationHandler(this).addChild(new SyncErrorFragment(), R.id.top_tooltip);
        }

        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigationHandler.navigateUpToTripsFragment();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (!navigationHandler.isDualPane()) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            } else {
                actionBar.setHomeButtonEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
        updateActionBarTitlePrice();
        tripTableController.subscribe(mActionBarTitleUpdatesListener);
    }

    @Override
    public void onPause() {
        tripTableController.unsubscribe(mActionBarTitleUpdatesListener);
        mLastTripController.setLastTrip(mTrip);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");

        Bundle extraState = new Bundle();
        extraState.putParcelable(KEY_OUT_TRIP, mTrip);
        fragmentStateCache.putSavedState(extraState, getClass());
    }

    @Override
    public void onDestroy() {
        fragmentStateCache.onDestroy(this);
        super.onDestroy();
    }

    @NonNull
    public Trip getTrip() {
        return mTrip;
    }

    private class ActionBarTitleUpdatesListener extends StubTableEventsListener<Trip> {

        @Override
        public void onGetSuccess(@NonNull List<Trip> list) {
            if (isAdded()) {
                if (list.contains(mTrip)) {
                    updateActionBarTitlePrice();
                }
            }
        }

        @Override
        public void onUpdateSuccess(@NonNull Trip oldTrip, @NonNull Trip newTrip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
            if (isAdded()) {
                if (mTrip.equals(oldTrip)) {
                    mTrip = newTrip;
                    mFragmentPagerAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void updateActionBarTitlePrice() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTrip.getPrice().getCurrencyFormattedPrice() + " - " + mTrip.getName());
        }
    }

}