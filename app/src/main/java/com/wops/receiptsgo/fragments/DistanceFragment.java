package com.wops.receiptsgo.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.R;
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.adapters.DistanceAdapter;
import com.wops.receiptsgo.adapters.DistanceListItem;
import com.wops.receiptsgo.databinding.ReportDistanceListBinding;
import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Price;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.factory.PriceBuilderFactory;
import com.wops.receiptsgo.model.utils.ModelUtils;
import com.wops.receiptsgo.persistence.database.controllers.TripForeignKeyTableEventsListener;
import com.wops.receiptsgo.persistence.database.controllers.impl.DistanceTableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.search.delegates.DoubleHeaderItem;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.receiptsgo.widget.ui.BottomSpacingItemDecoration;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kotlin.Pair;
import kotlin.Unit;

public class DistanceFragment extends WBFragment implements TripForeignKeyTableEventsListener<Distance>, FabClickListener {

    @Inject
    UserPreferenceManager preferenceManager;

    @Inject
    DistanceTableController distanceTableController;

    @Inject
    BackupProvidersManager backupProvidersManager;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    DateFormatter dateFormatter;

    private Trip trip;
    private DistanceAdapter distanceAdapter;
    private Distance lastInsertedDistance;

    private ReportDistanceListBinding binding;

    private String actionBarSubtitle = "";

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
        distanceAdapter = new DistanceAdapter(distance -> {
            navigationHandler.navigateToEditDistanceFragment(trip, distance);
            return Unit.INSTANCE;
        }, backupProvidersManager);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");
        binding = ReportDistanceListBinding.inflate(inflater, container, false);

        trip = ((ReportInfoFragment) getParentFragment()).getTrip();
        Preconditions.checkNotNull(trip, "A valid trip is required");
        binding.listDistances.setAdapter(distanceAdapter);
        binding.listDistances.addItemDecoration(new BottomSpacingItemDecoration());

        return binding.getRoot();
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
        setActionBarSubtitle(actionBarSubtitle);
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
    public void onFabClick() {
        navigationHandler.navigateToCreateNewDistanceFragment(trip, lastInsertedDistance == null ? null : lastInsertedDistance.getDate());
    }

    @Override
    public void onGetSuccess(@NonNull List<Distance> distances, @NonNull Trip trip) {
        if (isAdded()) {
            final List<DistanceListItem> resultList = new ArrayList<>();

            Observable.fromIterable(distances)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .sorted(Distance::compareTo)
                    .groupBy(distance -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date dateWithoutTime = sdf.parse(sdf.format(distance.getDate()));
                        return dateWithoutTime;
                    })
                    .sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey()))
                    .flatMap(dateDistanceGroupedObservable ->
                            dateDistanceGroupedObservable
                                    .toList()
                                    .map(distancesWithSameDate -> {
                                        final String sumPrice = new PriceBuilderFactory().setPriceables(distancesWithSameDate, trip.getTripCurrency()).build().getCurrencyFormattedPrice();
                                        final String date = dateFormatter.getFormattedDate(distancesWithSameDate.get(0).getDisplayableDate());
                                        return new Pair<>(new DoubleHeaderItem(date, sumPrice), distancesWithSameDate);
                                    })
                                    .toObservable()
                    )
                    .toList()
                    .subscribe(pairs -> {
                        // add content
                        for (Pair<DoubleHeaderItem, List<Distance>> pair : pairs) {
                            resultList.add(pair.component1());
                            resultList.addAll(pair.component2());
                        }

                        distanceAdapter.setItems(resultList);
                        distanceAdapter.notifyDataSetChanged();

                        if (binding != null) {
                            binding.progress.setVisibility(View.GONE);
                            if (distances.isEmpty()) {
                                binding.listDistances.setVisibility(View.GONE);
                                binding.noData.setVisibility(View.VISIBLE);
                            } else {
                                binding.noData.setVisibility(View.GONE);
                                binding.listDistances.setVisibility(View.VISIBLE);
                            }
                        }
                        updateSubtitle(distances);
                    });
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

    private void updateSubtitle(@NonNull List<Distance> allDistances) {

        Observable.fromIterable(allDistances)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(o -> o instanceof Distance)
                .toList()
                .map(distances -> {
                    if (preferenceManager.get(UserPreference.Distance.ShowDistanceAsPriceInSubtotal)) {
                        final Price price = new PriceBuilderFactory().setPriceables(distances, this.trip.getTripCurrency()).build();
                        return getString(R.string.distance_total_item, price.getCurrencyFormattedPrice());
                    } else {
                        BigDecimal distanceTotal = BigDecimal.ZERO;
                        for (final Distance distance : distances) {
                            distanceTotal = distanceTotal.add(distance.getDistance());
                        }
                        return getString(R.string.distance_total_item, ModelUtils.getDecimalFormattedValue(distanceTotal, Distance.DISTANCE_PRECISION));
                    }
                })
                .subscribe(subtitle -> {
                            actionBarSubtitle = subtitle;
                            setActionBarSubtitle(actionBarSubtitle);
                        }, throwable -> Logger.error(this, throwable.getMessage())
                );
    }

    private void setActionBarSubtitle(@NonNull String text) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && getUserVisibleHint()) {
            actionBar.setSubtitle(text);
        }
    }

    private void showToastMessage(int stringResId) {
        if (isAdded()) {
            Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        binding.listDistances.setAdapter(null);
        super.onDestroyView();
        binding = null;
    }
}
