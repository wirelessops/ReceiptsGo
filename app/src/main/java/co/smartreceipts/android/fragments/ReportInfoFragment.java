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
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipFragment;
import co.smartreceipts.android.widget.tooltip.report.backup.BackupNavigator;
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateNavigator;
import dagger.android.support.AndroidSupportInjection;

public class ReportInfoFragment extends WBFragment implements GenerateNavigator, BackupNavigator {

    public static final String TAG = ReportInfoFragment.class.getSimpleName();

    private static final String KEY_OUT_TRIP = "key_out_trip";

    @Inject
    ConfigurationManager configurationManager;
    @Inject
    TripTableController tripTableController;
    @Inject
    NavigationHandler navigationHandler;
    
    private LastTripController lastTripController;
    private TripFragmentPagerAdapter fragmentPagerAdapter;
    private Trip trip;
    private ActionBarTitleUpdatesListener actionBarTitleUpdatesListener;

    private ViewPager viewPager;
    private PagerSlidingTabStrip pagerSlidingTabStrip;

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
            trip = getArguments().getParcelable(Trip.PARCEL_KEY);
        } else {
            trip = savedInstanceState.getParcelable(KEY_OUT_TRIP);
        }
        Preconditions.checkNotNull(trip, "A valid trip is required");
        lastTripController = new LastTripController(getActivity());
        fragmentPagerAdapter = new TripFragmentPagerAdapter(getResources(), getChildFragmentManager(),
                configurationManager);
        actionBarTitleUpdatesListener = new ActionBarTitleUpdatesListener();
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
            new ChildFragmentNavigationHandler(this).addChild(ReportTooltipFragment.newInstance(), R.id.top_tooltip);
        }

        viewPager = view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(2); // Set this to 2, since we're always two away from the receipts page
        pagerSlidingTabStrip = view.findViewById(R.id.tabs);

        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCurrentItem(fragmentPagerAdapter.getReceiptsTabPosition());
        
        pagerSlidingTabStrip.setViewPager(viewPager);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
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
        tripTableController.subscribe(actionBarTitleUpdatesListener);
    }

    @Override
    public void onPause() {
        tripTableController.unsubscribe(actionBarTitleUpdatesListener);
        lastTripController.setLastTrip(trip);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putParcelable(KEY_OUT_TRIP, trip);
    }

    @NonNull
    public Trip getTrip() {
        return trip;
    }

    private class ActionBarTitleUpdatesListener extends StubTableEventsListener<Trip> {

        @Override
        public void onGetSuccess(@NonNull List<Trip> list) {
            if (isAdded()) {
                if (list.contains(trip)) {
                    updateActionBarTitlePrice();
                }
            }
        }

        @Override
        public void onUpdateSuccess(@NonNull Trip oldTrip, @NonNull Trip newTrip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
            if (isAdded()) {
                if (trip.equals(oldTrip)) {
                    trip = newTrip;
                    fragmentPagerAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void updateActionBarTitlePrice() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(trip.getPrice().getCurrencyFormattedPrice() + " - " + trip.getName());
        }
    }

    @Override
    public void navigateToGenerateTab() {
        viewPager.setCurrentItem(fragmentPagerAdapter.getGenerateTabPosition(), true);
    }

    @Override
    public void navigateToBackup() {
        navigationHandler.navigateToBackupMenu();
    }
}