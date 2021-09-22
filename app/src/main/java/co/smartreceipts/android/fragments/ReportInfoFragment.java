package co.smartreceipts.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.ReportInfoFragmentPagerAdapter;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.databinding.ReportInfoViewPagerBinding;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.LastTripMonitor;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipFragment;
import co.smartreceipts.android.widget.tooltip.report.backup.BackupNavigator;
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateNavigator;
import dagger.android.support.AndroidSupportInjection;

public class ReportInfoFragment extends WBFragment implements GenerateNavigator, BackupNavigator, View.OnClickListener {

    public static final String TAG = ReportInfoFragment.class.getSimpleName();

    private static final String KEY_OUT_TRIP = "key_out_trip";

    @Inject
    ConfigurationManager configurationManager;

    @Inject
    TripTableController tripTableController;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    DatabaseHelper database;

    @Inject
    UserPreferenceManager userPreferenceManager;

    private LastTripMonitor lastTripMonitor;
    private ReportInfoFragmentPagerAdapter fragmentPagerAdapter;
    private Trip trip;
    private ActionBarTitleUpdatesListener actionBarTitleUpdatesListener;

    private ReportInfoViewPagerBinding binding;

    private ArrayList<ImageView> bottomNavigationIcons;

    @NonNull
    public static ReportInfoFragment newInstance() {
        return new ReportInfoFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
        lastTripMonitor = new LastTripMonitor(getActivity());
        fragmentPagerAdapter = new ReportInfoFragmentPagerAdapter(getChildFragmentManager(), configurationManager);
        actionBarTitleUpdatesListener = new ActionBarTitleUpdatesListener();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ReportInfoViewPagerBinding.inflate(inflater, container, false);

        binding.fab.setOnClickListener(v -> {
            Fragment currentPage = getCurrentViewPagerFragment();
            if (currentPage instanceof FabClickListener) {
                ((FabClickListener) currentPage).onFabClick();
            }
        });

        return binding.getRoot();
    }

    private Fragment getCurrentViewPagerFragment() {
        return getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + binding.pager.getCurrentItem());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            new ChildFragmentNavigationHandler(this).addChild(ReportTooltipFragment.newInstance(), R.id.top_tooltip);
        }

        binding.pager.setOffscreenPageLimit(3); // Set this to 3, since we have 4 tabs

        bottomNavigationIcons = new ArrayList<>(fragmentPagerAdapter.getCount());

        // receipts
        bottomNavigationIcons.add(fragmentPagerAdapter.getReceiptsTabPosition(), binding.bottomAppBar.navigationReceipts);
        // distances
        if (fragmentPagerAdapter.getDistancesTabPosition() != -1) {
            bottomNavigationIcons.add(fragmentPagerAdapter.getDistancesTabPosition(), binding.bottomAppBar.navigationDistances);
        } else {
            binding.bottomAppBar.navigationDistances.setVisibility(View.GONE);
        }
        // generate
        bottomNavigationIcons.add(fragmentPagerAdapter.getGenerateTabPosition(), binding.bottomAppBar.navigationGenerate);
        // graphs
        if (fragmentPagerAdapter.getGraphsTabPosition() != -1) {
            bottomNavigationIcons.add(fragmentPagerAdapter.getGraphsTabPosition(), binding.bottomAppBar.navigationGraphs);
        } else {
            binding.bottomAppBar.navigationGraphs.setVisibility(View.GONE);
        }

        for (int i = 0; i < bottomNavigationIcons.size(); i++) {
            bottomNavigationIcons.get(i).setOnClickListener(this);
        }

        binding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setActiveBottomNavigationPosition(position);
            }
        });

        binding.pager.setAdapter(fragmentPagerAdapter);
        final int receiptsTabPosition = fragmentPagerAdapter.getReceiptsTabPosition();
        binding.pager.setCurrentItem(receiptsTabPosition);
        setActiveBottomNavigationPosition(receiptsTabPosition);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Toolbar toolbar = navigationHandler.isDualPane() ? getActivity().findViewById(R.id.toolbar) : binding.toolbar;
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigationHandler.navigateToHomeTripsFragment();
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
        lastTripMonitor.setLastTrip(trip);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putParcelable(KEY_OUT_TRIP, trip);
    }

    @NonNull
    public Trip getTrip() {
        return trip;
    }

    @Override
    public void onClick(View view) {
        setActiveBottomNavigationPosition(bottomNavigationIcons.indexOf(view));

        if (view.getId() == R.id.navigation_receipts) {
            navigateToReceiptsTab();
        } else if (view.getId() == R.id.navigation_distances) {
            navigateToDistancesTab();
        } else if (view.getId() == R.id.navigation_generate) {
            navigateToGenerateTab();
        } else if (view.getId() == R.id.navigation_graphs) {
            navigateToGraphsTab();
        }
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
            final List<Receipt> receipts = new ArrayList<>(database.getReceiptsTable().getBlocking(trip, false));
            ArrayList<Price> total = new ArrayList<>();
            if (!userPreferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable)) {
                for (Receipt receipt : receipts) {
                    total.add(receipt.getPrice());
                    if (userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)) {
                        total.add(receipt.getTax());
                    }
                }
            } else {
                for (Receipt receipt : receipts) {
                    if (receipt.isReimbursable()) {
                        total.add(receipt.getPrice());
                        if (userPreferenceManager.get(UserPreference.Receipts.UsePreTaxPrice)) {
                            total.add(receipt.getTax());
                        }
                    }
                }
            }
            Price totalPrice = new PriceBuilderFactory().setPrices(total, trip.getTripCurrency()).build();
            actionBar.setTitle(totalPrice.getCurrencyFormattedPrice() + " - " + trip.getName());
        }
    }

    @Override
    public void navigateToGenerateTab() {
        binding.pager.setCurrentItem(fragmentPagerAdapter.getGenerateTabPosition(), true);
    }

    private void navigateToReceiptsTab() {
        binding.pager.setCurrentItem(fragmentPagerAdapter.getReceiptsTabPosition());
    }

    private void navigateToDistancesTab() {
        binding.pager.setCurrentItem(fragmentPagerAdapter.getDistancesTabPosition());
    }

    private void navigateToGraphsTab() {
        binding.pager.setCurrentItem(fragmentPagerAdapter.getGraphsTabPosition());
    }

    @Override
    public void navigateToBackup() {
        navigationHandler.navigateToBackupMenu();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bottomNavigationIcons = null;
        binding = null;
    }

    private void setActiveBottomNavigationPosition(int position) {
        final int colorActive = requireContext().getResources().getColor(R.color.navigation_active);
        final int colorInactive = requireContext().getResources().getColor(R.color.navigation_inactive);

        for (int i = 0; i < bottomNavigationIcons.size(); i++) {
            if (position == i) {
                bottomNavigationIcons.get(i).setColorFilter(colorActive);
            } else {
                bottomNavigationIcons.get(i).setColorFilter(colorInactive);
            }
        }

        if (position == fragmentPagerAdapter.getGraphsTabPosition()) {
            binding.fab.hide();
            binding.bottomAppBar.placeholder.setVisibility(View.GONE);
        } else if (position == fragmentPagerAdapter.getReceiptsTabPosition() || position == fragmentPagerAdapter.getDistancesTabPosition()) {
            binding.fab.setImageResource(R.drawable.ic_add_24dp);
            binding.fab.show();
            binding.bottomAppBar.placeholder.setVisibility(View.VISIBLE);
        } else if (position == fragmentPagerAdapter.getGenerateTabPosition()) {
            binding.fab.setImageResource(R.drawable.ic_share);
            binding.fab.show();
            binding.bottomAppBar.placeholder.setVisibility(View.VISIBLE);
        }

        binding.bottomAppBar.getRoot().performShow();
    }
}