package co.smartreceipts.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.adapters.DistanceAdapter;
import co.smartreceipts.android.distance.editor.DistanceDialogFragment;
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
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.support.AndroidSupportInjection;

public class DistanceFragment extends WBListFragment implements TripForeignKeyTableEventsListener<Distance> {

    @Inject
    UserPreferenceManager preferenceManager;
    @Inject
    DistanceTableController distanceTableController;
    @Inject
    BackupProvidersManager backupProvidersManager;

    private Trip trip;
    private DistanceAdapter distanceAdapter;
    private View progressDialog;
    private TextView noDataAlert;
    private Distance lastInsertedDistance;

    public static DistanceFragment newInstance() {
        return new DistanceFragment();
    }


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");
        distanceAdapter = new DistanceAdapter(getActivity(), preferenceManager,
                backupProvidersManager);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");
        final View view = inflater.inflate(R.layout.report_distance_list, container, false);
        progressDialog = view.findViewById(R.id.progress);
        noDataAlert = (TextView) view.findViewById(R.id.no_data);
        noDataAlert.setText(R.string.distance_no_data);
        view.findViewById(R.id.distance_action_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DistanceDialogFragment dialog = (lastInsertedDistance == null) ? DistanceDialogFragment.newInstance(trip) : DistanceDialogFragment.newInstance(trip, lastInsertedDistance.getDate());
                dialog.show(getFragmentManager(), DistanceDialogFragment.TAG);
            }
        });
        return view;
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
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");
        distanceTableController.subscribe(this);
        distanceTableController.get(trip);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null && isVisibleToUser) {
            // Refresh as soon as we're visible
            distanceTableController.get(trip);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.debug(this, "onPause");
        distanceTableController.unsubscribe(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Distance distance = distanceAdapter.getItem(position);
        final DistanceDialogFragment dialog = DistanceDialogFragment.newInstance(trip, distance);
        dialog.show(getFragmentManager(), DistanceDialogFragment.TAG);
        getFragmentManager().executePendingTransactions();
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

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null && getUserVisibleHint()) {
                if (preferenceManager.get(UserPreference.Distance.ShowDistanceAsPriceInSubtotal)) {
                    final Price total = new PriceBuilderFactory().setPriceables(distances, this.trip.getTripCurrency()).build();
                    getSupportActionBar().setSubtitle(getString(R.string.distance_total_item, total.getCurrencyFormattedPrice()));
                } else {
                    BigDecimal distanceTotal = new BigDecimal(0);
                    for (final Distance distance : distances) {
                        distanceTotal = distanceTotal.add(distance.getDistance());
                    }
                    getSupportActionBar().setSubtitle(getString(R.string.distance_total_item, ModelUtils.getDecimalFormattedValue(distanceTotal)));
                }
            }
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

    private void showToastMessage(int stringResId) {
        if (isAdded()) {
            Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
        }
    }
}
