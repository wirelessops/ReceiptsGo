package co.smartreceipts.android.trips;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.TripCardAdapter;
import co.smartreceipts.android.fragments.WBListFragment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.LastTripController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.receipts.ReceiptsFragment;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.tooltip.StaticTooltipView;
import co.smartreceipts.android.tooltip.TooltipPresenter;
import co.smartreceipts.android.tooltip.model.StaticTooltip;
import co.smartreceipts.android.utils.ConfigurableStaticFeature;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.tooltip.Tooltip;
import co.smartreceipts.android.workers.EmailAssistant;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.flex.Flex;

public class TripFragment extends WBListFragment implements TableEventsListener<Trip>,
        AdapterView.OnItemLongClickListener, StaticTooltipView {

    public static final String ARG_NAVIGATE_TO_VIEW_LAST_TRIP = "arg_nav_to_last_trip";
    private static final String OUT_NAV_TO_LAST_TRIP = "out_nav_to_last_trip";
    private static final String OUT_SELECTED_TRIP = "out_selected_trip";

    @Inject
    Flex flex;

    @Inject
    TripTableController tripTableController;

    @Inject
    BackupProvidersManager backupProvidersManager;

    @Inject
    UserPreferenceManager preferenceManager;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    TooltipPresenter tooltipPresenter;

    private TripCardAdapter tripCardAdapter;
    private ProgressBar progressBar;
    private TextView noDataAlert;
    private Tooltip tooltipView;

    private boolean navigateToLastTrip;

    private Trip selectedTrip;


    public static TripFragment newInstance() {
        return new TripFragment();
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
        tripCardAdapter = new TripCardAdapter(getActivity(), preferenceManager, backupProvidersManager);
        if (savedInstanceState == null) {
            navigateToLastTrip = getArguments().getBoolean(ARG_NAVIGATE_TO_VIEW_LAST_TRIP);
        } else {
            navigateToLastTrip = savedInstanceState.getBoolean(OUT_NAV_TO_LAST_TRIP);
            selectedTrip = savedInstanceState.getParcelable(OUT_SELECTED_TRIP);
            if (navigationHandler.isDualPane()) {
                tripCardAdapter.setSelectedItem(selectedTrip);
            }
        }
        tripTableController.subscribe(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");
        final View rootView = inflater.inflate(R.layout.trip_fragment_layout, container, false);
        progressBar = rootView.findViewById(R.id.progress);
        noDataAlert = rootView.findViewById(R.id.no_data);
        tooltipView = rootView.findViewById(R.id.trip_tooltip);
        rootView.findViewById(R.id.trip_action_new).setOnClickListener(v -> tripMenu(null));
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(tripCardAdapter); // Set this here to ensure this has been laid out already
        getListView().setOnItemLongClickListener(this);
        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.debug(this, "onStart");
        tooltipPresenter.subscribe(); // Note: Keep this tied to onStart due to Android 7.x TransitionLibrary bugs
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");
        tripTableController.get();
        getActivity().setTitle(getFlexString(R.string.sr_app_name));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void onStop() {
        Logger.debug(this, "onStop");
        tooltipPresenter.unsubscribe();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        tripTableController.unsubscribe(this);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putBoolean(OUT_NAV_TO_LAST_TRIP, navigateToLastTrip);
        outState.putParcelable(OUT_SELECTED_TRIP, selectedTrip);
    }

    public final void tripMenu(final Trip trip) {
        if (trip == null) {
            navigationHandler.navigateToCreateTripFragment(tripCardAdapter.getData());
        } else {
            navigationHandler.navigateToEditTripFragment(trip);
        }
    }

    public final boolean editTrip(final Trip trip) {
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        final String[] editTripItems = flex.getStringArray(getActivity(), R.array.EDIT_TRIP_ITEMS);
        builder.setTitle(trip.getName())
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.cancel())
                .setItems(editTripItems, (dialog, item) -> {
                    final String selection = editTripItems[item];
                    if (selection == editTripItems[0]) {
                        TripFragment.this.tripMenu(trip);
                    } else if (selection == editTripItems[1]) {
                        TripFragment.this.deleteTrip(trip);
                    }
                    dialog.cancel();
                }).show();
        return true;
    }

    public final void deleteTrip(final Trip trip) {
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.delete_item, trip.getName()))
                .setMessage(getString(R.string.delete_sync_information))
                .setCancelable(true)
                .setPositiveButton(getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON), (dialog, id) -> tripTableController.delete(trip, new DatabaseOperationMetadata()))
                .setNegativeButton(getFlexString(R.string.DIALOG_CANCEL), (dialog, id) -> dialog.cancel()).show();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        viewReceipts(tripCardAdapter.getItem(position));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
        editTrip(tripCardAdapter.getItem(position));
        return true;
    }

    @Override
    public void onGetSuccess(@NonNull List<Trip> trips) {
        if (isResumed()) {
            progressBar.setVisibility(View.GONE);
            getListView().setVisibility(View.VISIBLE);
            if (trips.isEmpty()) {
                noDataAlert.setVisibility(View.VISIBLE);
            } else {
                noDataAlert.setVisibility(View.INVISIBLE);
            }
            tripCardAdapter.notifyDataSetChanged(trips);

            if (!trips.isEmpty() && navigateToLastTrip && ConfigurableStaticFeature.AutomaticallyLaunchLastTrip.isEnabled(getContext())) {
                navigateToLastTrip = false;
                // If we have trips, open up whatever one was last used
                final LastTripController lastTripController = new LastTripController(getActivity());
                final Trip lastTrip = lastTripController.getLastTrip(trips);
                if (lastTrip != null) {
                    viewReceipts(lastTrip);
                }
            }
        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {
        if (isResumed()) {
            if (e instanceof SQLiteDatabaseCorruptException) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_sql_corrupt_title).setMessage(R.string.dialog_sql_corrupt_message).setPositiveButton(R.string.dialog_sql_corrupt_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.dialog_sql_corrupt_intent_subject), getString(R.string.dialog_sql_corrupt_intent_text));
                        getActivity().startActivity(Intent.createChooser(intent, getResources().getString(R.string.dialog_sql_corrupt_chooser)));
                        dialog.dismiss();
                    }
                }).show();
            } else {
                Toast.makeText(getActivity(), R.string.database_get_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onInsertSuccess(@NonNull Trip trip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            tripTableController.get();
            viewReceipts(trip);
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
            viewReceipts(newTrip);
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
                    actionBar.setTitle(getFlexString(R.string.sr_app_name));
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
    public List<StaticTooltip> getSupportedTooltips() {
        return Arrays.asList(StaticTooltip.PrivacyPolicy, StaticTooltip.RateThisApp);
    }

    @Override
    public void display(@NotNull StaticTooltip tooltip) {
        tooltipView.setTooltip(tooltip);
        if (tooltipView.getVisibility() != View.VISIBLE) {
            tooltipView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideTooltip() {
        if (tooltipView.getVisibility() != View.GONE) {
            tooltipView.setVisibility(View.GONE);
        }
    }

    @NotNull
    @Override
    public Observable<Object> getTooltipClickStream() {
        return tooltipView.getTooltipClickStream();
    }

    @NotNull
    @Override
    public Observable<Object> getButtonNoClickStream() {
        return tooltipView.getButtonNoClickStream();
    }

    @NotNull
    @Override
    public Observable<Object> getButtonYesClickStream() {
        return tooltipView.getButtonYesClickStream();
    }

    @NotNull
    @Override
    public Observable<Object> getButtonCancelClickStream() {
        return tooltipView.getButtonCancelClickStream();
    }

    @NotNull
    @Override
    public Observable<Object> getCloseIconClickStream() {
        return tooltipView.getCloseIconClickStream();
    }

    private void viewReceipts(Trip trip) {
        selectedTrip = trip;
        if (navigationHandler.isDualPane()) {
            tripCardAdapter.setSelectedItem(trip);
        }
        navigationHandler.navigateToReportInfoFragment(trip);
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }
}