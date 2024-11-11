package com.wops.receiptsgo.trips;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.analytics.log.Logger;
import com.wops.receiptsgo.R;
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.adapters.TripAdapter;
import com.wops.receiptsgo.databinding.TripsFragmentLayoutBinding;
import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.fragments.WBFragment;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.LastTripMonitor;
import com.wops.receiptsgo.persistence.database.controllers.TableEventsListener;
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.purchases.plus.SmartReceiptsTitle;
import com.wops.receiptsgo.receipts.ReceiptsFragment;
import com.wops.receiptsgo.search.SearchResultKeeper;
import com.wops.receiptsgo.search.Searchable;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.receiptsgo.tooltip.TooltipPresenter;
import com.wops.receiptsgo.tooltip.TooltipView;
import com.wops.receiptsgo.tooltip.model.TooltipMetadata;
import com.wops.receiptsgo.tooltip.model.TooltipType;
import com.wops.receiptsgo.trips.editor.TripEditOption;
import com.wops.receiptsgo.trips.editor.TripEditOptionsDialog;
import com.wops.receiptsgo.trips.navigation.LastTripAutoNavigationController;
import com.wops.receiptsgo.trips.navigation.LastTripAutoNavigationTracker;
import com.wops.receiptsgo.trips.navigation.NewTripAutoNavigationTracker;
import com.wops.receiptsgo.trips.navigation.ViewReceiptsInTripRouter;
import com.wops.receiptsgo.workers.EmailAssistant;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import kotlin.Unit;
import wb.android.flex.Flex;

public class TripFragment extends WBFragment implements TableEventsListener<Trip>, TooltipView, ViewReceiptsInTripRouter {

    private static final String OUT_SELECTED_TRIP = "out_selected_trip";

    @Inject
    Flex flex;

    @Inject
    SmartReceiptsTitle smartReceiptsTitle;

    @Inject
    TripTableController tripTableController;

    @Inject
    BackupProvidersManager backupProvidersManager;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    TooltipPresenter tooltipPresenter;

    @Inject
    LastTripAutoNavigationController lastTripAutoNavigationController;

    @Inject
    LastTripMonitor lastTripMonitor;

    @Inject
    LastTripAutoNavigationTracker lastTripAutoNavigationTracker;

    @Inject
    NewTripAutoNavigationTracker newTripAutoNavigationTracker;

    @Inject
    DateFormatter dateFormatter;

    private TripAdapter tripCardAdapter;

    private Trip selectedTrip = null;

    private boolean hasResults = false;

    private TripsFragmentLayoutBinding binding;

    public static TripFragment newInstance() {
        return new TripFragment();
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

        tripCardAdapter = new TripAdapter(
                trip -> {
                    routeToViewReceipts(trip);
                    return Unit.INSTANCE;
                },
                trip -> {
                    showTripEditOptionsDialog(trip);
                    return Unit.INSTANCE;
                },
                dateFormatter, backupProvidersManager);

        if (savedInstanceState != null) {
            selectedTrip = savedInstanceState.getParcelable(OUT_SELECTED_TRIP);
            if (navigationHandler.isDualPane() && selectedTrip != null) {
                tripCardAdapter.setSelectedItemId(selectedTrip.getId());
            }
        }

        // listening for edit trip options
        getChildFragmentManager().setFragmentResultListener(TripEditOptionsDialog.REQUEST_KEY, this,
                (requestKey, result) -> {
                    String editOption = result.getString(TripEditOptionsDialog.RESULT_KEY);

                    if (editOption.equals(TripEditOption.EDIT.name())) {
                        tripMenu(selectedTrip);
                    } else if (editOption.equals(TripEditOption.DELETE.name())) {
                        deleteTrip(selectedTrip);
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");
        binding = TripsFragmentLayoutBinding.inflate(inflater, container, false);
        binding.layoutTripCardList.tripActionNew.setOnClickListener(v -> tripMenu(null));
        binding.layoutTripCardList.list.setAdapter(tripCardAdapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Toolbar toolbar = (navigationHandler.isDualPane()) ? getActivity().findViewById(R.id.toolbar) : binding.toolbar.toolbar;
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.debug(this, "onStart");
        tooltipPresenter.subscribe();
        tripTableController.subscribe(this);
        tripTableController.get();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");
        requireActivity().setTitle(smartReceiptsTitle.get());
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setSubtitle(null);
        }
        lastTripAutoNavigationController.subscribe();
        if (hasResults) {
            updateViewVisibilities(tripCardAdapter.getItems());
        }

        newTripAutoNavigationTracker.subscribe(this);
    }

    @Override
    public void onPause() {
        lastTripAutoNavigationController.unsubscribe();
        newTripAutoNavigationTracker.dispose();
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.debug(this, "onStop");
        tooltipPresenter.unsubscribe();
        tripTableController.unsubscribe(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        binding.layoutTripCardList.list.setAdapter(null);
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putParcelable(OUT_SELECTED_TRIP, selectedTrip);
    }

    public final void tripMenu(@Nullable final Trip trip) {
        if (trip == null) {
            navigationHandler.navigateToCreateTripFragment(tripCardAdapter.getItems());
        } else {
            navigationHandler.navigateToEditTripFragment(trip);
        }
    }

    private void showTripEditOptionsDialog(@NonNull final Trip trip) {
        selectedTrip = trip;

        TripEditOptionsDialog dialog = TripEditOptionsDialog.newInstance(trip.getName());
        dialog.show(getChildFragmentManager(), TripEditOptionsDialog.TAG);
    }


    private void deleteTrip(@NonNull final Trip trip) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete_item, trip.getName()))
                .setMessage(getString(R.string.delete_sync_information))
                .setCancelable(true)
                .setPositiveButton(getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON), (dialog, id) -> tripTableController.delete(trip, new DatabaseOperationMetadata()))
                .setNegativeButton(getFlexString(R.string.DIALOG_CANCEL), (dialog, id) -> dialog.cancel()).show();
    }

    @Override
    public void onGetSuccess(@NonNull List<Trip> trips) {
        if (isAdded()) {
            updateViewVisibilities(trips);
            hasResults = true;
            tripCardAdapter.setItems(trips);
            tripCardAdapter.notifyDataSetChanged();

            if (getActivity() instanceof SearchResultKeeper) {
                final SearchResultKeeper searchResultKeeper = (SearchResultKeeper) getActivity();

                final Searchable searchResult = searchResultKeeper.getSearchResult();
                if (searchResult instanceof Trip) {
                    final int index = trips.indexOf(searchResult);
                    if (index >= 0) {
                        binding.layoutTripCardList.list.smoothScrollToPosition(index);
                    }

                    searchResultKeeper.markSearchResultAsProcessed();
                }
            }
        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {
        if (isResumed()) {
            if (e instanceof SQLiteDatabaseCorruptException) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_sql_corrupt_title)
                        .setMessage(R.string.dialog_sql_corrupt_message)
                        .setPositiveButton(R.string.dialog_sql_corrupt_positive, (dialog, position) -> {
                                    Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.dialog_sql_corrupt_intent_subject), getString(R.string.dialog_sql_corrupt_intent_text));
                                    getActivity().startActivity(Intent.createChooser(intent, getResources().getString(R.string.dialog_sql_corrupt_chooser)));
                                    dialog.dismiss();
                                }
                        ).show();
            } else {
                Toast.makeText(getActivity(), R.string.database_get_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onInsertSuccess(@NonNull Trip trip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        // trying to reduce calling tableController.get(). On phones it'll be called from onStart(), but need to call it manually here for tablets
        if (isResumed() && navigationHandler.isDualPane()) {
            tripTableController.get();
        }
    }

    @Override
    public void onInsertFailure(@NonNull Trip trip, @Nullable Throwable ex, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            if (ex != null) {
                Toast.makeText(getActivity(), R.string.toast_error_trip_exists, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Trip oldTip, @NonNull Trip newTrip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            routeToViewReceipts(newTrip);
        }
    }

    @Override
    public void onUpdateFailure(@NonNull Trip oldTrip, @Nullable Throwable ex, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            if (ex != null) {
                Toast.makeText(getActivity(), R.string.toast_error_trip_exists, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull Trip oldTrip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            final Fragment detailsFragment = getFragmentManager().findFragmentByTag(ReceiptsFragment.TAG);
            if (detailsFragment != null) {
                getFragmentManager().beginTransaction().remove(detailsFragment).commit();
                final ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(smartReceiptsTitle.get());
                }
            }
        }
        tripTableController.get();
    }

    @Override
    public void onDeleteFailure(@NonNull Trip oldTrip, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_LONG).show();
        }
    }

    @NotNull
    @Override
    public List<TooltipType> getSupportedTooltips() {
        return Arrays.asList(TooltipType.AutomaticBackupRecoveryHint, TooltipType.PrivacyPolicy, TooltipType.RateThisApp);
    }

    @Override
    public void display(@NotNull TooltipMetadata tooltip) {
        binding.tripTooltip.setTooltip(tooltip);
        if (binding.tripTooltip.getVisibility() != View.VISIBLE) {
            binding.tripTooltip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideTooltip() {
        if (binding.tripTooltip.getVisibility() != View.GONE) {
            binding.tripTooltip.setVisibility(View.GONE);
        }
    }

    @NotNull
    @Override
    public Observable<Unit> getTooltipClickStream() {
        return binding.tripTooltip.getTooltipClickStream();
    }

    @NotNull
    @Override
    public Observable<Unit> getButtonNoClickStream() {
        return binding.tripTooltip.getButtonNoClickStream();
    }

    @NotNull
    @Override
    public Observable<Unit> getButtonYesClickStream() {
        return binding.tripTooltip.getButtonYesClickStream();
    }

    @NotNull
    @Override
    public Observable<Unit> getButtonCancelClickStream() {
        return binding.tripTooltip.getButtonCancelClickStream();
    }

    @NotNull
    @Override
    public Observable<Unit> getCloseIconClickStream() {
        return binding.tripTooltip.getCloseIconClickStream();
    }

    @Override
    public void routeToViewReceipts(@NotNull Trip trip) {
        if (isResumed()) {
            selectedTrip = trip;
            if (navigationHandler.isDualPane()) {
                tripCardAdapter.setSelectedItemId(trip.getId());
            }
            lastTripMonitor.setLastTrip(trip);
            lastTripAutoNavigationTracker.setHasNavigatedToLastTrip(true);
            navigationHandler.navigateToReportInfoFragment(trip);
        }
    }

    private void updateViewVisibilities(List<Trip> trips) {
        binding.layoutTripCardList.progress.setVisibility(View.GONE);
        binding.layoutTripCardList.list.setVisibility(View.VISIBLE);

        binding.layoutTripCardList.noData.setVisibility(trips.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }
}