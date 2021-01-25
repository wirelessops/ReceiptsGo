package co.smartreceipts.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.DistanceAdapter;
import co.smartreceipts.android.databinding.ReportDistanceListBinding;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.database.controllers.TripForeignKeyTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.BackupProvidersManager;
import dagger.android.support.AndroidSupportInjection;

public class DistanceFragment extends WBListFragment implements TripForeignKeyTableEventsListener<Distance> {

    @Inject
    UserPreferenceManager preferenceManager;

    @Inject
    DistanceTableController distanceTableController;

    @Inject
    BackupProvidersManager backupProvidersManager;

    @Inject
    NavigationHandler navigationHandler;

    private Trip trip;
    private DistanceAdapter distanceAdapter;
    private View progressDialog;
    private TextView noDataAlert;
    private Distance lastInsertedDistance;

    private ReportDistanceListBinding binding;

    public static DistanceFragment newInstance() {
        return new DistanceFragment();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");
        distanceAdapter = new DistanceAdapter(requireContext(), preferenceManager, backupProvidersManager);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");
        binding = ReportDistanceListBinding.inflate(inflater, container, false);
        progressDialog = binding.progress;
        noDataAlert = binding.noData;
        binding.distanceActionNew.setOnClickListener(v ->
                navigationHandler.navigateToCreateNewDistanceFragment(trip, lastInsertedDistance == null ? null : lastInsertedDistance.getDate()));
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.debug(this, "onActivityCreated");
        trip = ((ReportInfoFragment) getParentFragment()).getTrip();
        Preconditions.checkNotNull(trip, "A valid trip is required");
        setListAdapter(distanceAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.debug(this, "onStart");
        distanceTableController.subscribe(this);
        distanceTableController.get(trip);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        updateSubtitle();
    }

    @Override
    public void onStop() {
        distanceTableController.unsubscribe(this);
        Logger.debug(this, "onStop");
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        final Distance distance = distanceAdapter.getItem(position);
        navigationHandler.navigateToEditDistanceFragment(trip, distance);
    }

    @Override
    public void onGetSuccess(@NonNull List<Distance> distances, @NonNull Trip trip) {
        if (isAdded()) {
            distanceAdapter.notifyDataSetChanged(distances);
            progressDialog.setVisibility(View.GONE);
            if (distances.isEmpty()) {
                getListView().setVisibility(View.GONE);
                noDataAlert.setVisibility(View.VISIBLE);
            } else {
                noDataAlert.setVisibility(View.GONE);
                getListView().setVisibility(View.VISIBLE);
            }
            updateSubtitle();
        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e, @NonNull Trip trip) {
        // TODO: Respond?
    }

    @Override
    public void onGetSuccess(@NonNull List<Distance> list) {
        // TODO: Respond?
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {
        // TODO: Respond?
    }

    @Override
    public void onInsertSuccess(@NonNull Distance distance, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            distanceTableController.get(trip);
        }
        lastInsertedDistance = distance;
    }

    @Override
    public void onInsertFailure(@NonNull Distance distance, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        showToastMessage(R.string.distance_insert_failed);
    }

    @Override
    public void onUpdateSuccess(@NonNull Distance oldDistance, @NonNull Distance newDistance, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            distanceTableController.get(trip);
        }
    }

    @Override
    public void onUpdateFailure(@NonNull Distance oldDistance, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        showToastMessage(R.string.distance_update_failed);
    }

    @Override
    public void onDeleteSuccess(@NonNull Distance distance, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            distanceTableController.get(trip);
        }
    }

    @Override
    public void onDeleteFailure(@NonNull Distance distance, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        showToastMessage(R.string.distance_delete_failed);
    }

    private void updateSubtitle() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && getUserVisibleHint()) {
            final List<Distance> distances = distanceAdapter.getData();
            if (preferenceManager.get(UserPreference.Distance.ShowDistanceAsPriceInSubtotal)) {
                final Price total = new PriceBuilderFactory().setPriceables(distances, this.trip.getTripCurrency()).build();
                actionBar.setSubtitle(getString(R.string.distance_total_item, total.getCurrencyFormattedPrice()));
            } else {
                BigDecimal distanceTotal = BigDecimal.ZERO;
                for (final Distance distance : distances) {
                    distanceTotal = distanceTotal.add(distance.getDistance());
                }
                actionBar.setSubtitle(getString(R.string.distance_total_item, ModelUtils.getDecimalFormattedValue(distanceTotal, Distance.DISTANCE_PRECISION)));
            }
        }
    }

    private void showToastMessage(int stringResId) {
        if (isAdded()) {
            Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
