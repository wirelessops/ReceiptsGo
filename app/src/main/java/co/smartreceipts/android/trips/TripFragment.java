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

import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.TripCardAdapter;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.fragments.WBListFragment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.LastTripController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.rating.FeedbackDialogFragment;
import co.smartreceipts.android.rating.RatingDialogFragment;
import co.smartreceipts.android.receipts.ReceiptsFragment;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.utils.cache.FragmentArgumentCache;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.Tooltip;
import co.smartreceipts.android.workers.EmailAssistant;
import dagger.android.support.AndroidSupportInjection;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.flex.Flex;

public class TripFragment extends WBListFragment implements TableEventsListener<Trip>, AdapterView.OnItemLongClickListener {

    public static final String ARG_NAVIGATE_TO_VIEW_LAST_TRIP = "arg_nav_to_last_trip";
    private static final String OUT_NAV_TO_LAST_TRIP = "out_nav_to_last_trip";

    @Inject
    Flex flex;
    @Inject
    Analytics analytics;
    @Inject
    TripTableController tripTableController;
    @Inject
    BackupProvidersManager backupProvidersManager;
    @Inject
    UserPreferenceManager preferenceManager;
    @Inject
    FragmentArgumentCache fragmentArgumentCache;
    @Inject
    NavigationHandler navigationHandler;

    @Inject
    TripFragmentPresenter presenter;

    private TripCardAdapter tripCardAdapter;
    private ProgressBar progressBar;
    private TextView noDataAlert;
    private Tooltip tooltip;

    private boolean navigateToLastTrip;

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
            navigateToLastTrip = fragmentArgumentCache.get(TripFragment.class).getBoolean(ARG_NAVIGATE_TO_VIEW_LAST_TRIP);
        } else {
            navigateToLastTrip = savedInstanceState.getBoolean(OUT_NAV_TO_LAST_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");
        final View rootView = inflater.inflate(R.layout.trip_fragment_layout, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        noDataAlert = (TextView) rootView.findViewById(R.id.no_data);
        tooltip = (Tooltip) rootView.findViewById(R.id.trip_tooltip);
        rootView.findViewById(R.id.trip_action_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripMenu(null);
            }
        });
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(tripCardAdapter); // Set this here to ensure this has been laid out already
        getListView().setOnItemLongClickListener(this);
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        presenter.checkRating();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");
        tripTableController.subscribe(this);
        tripTableController.get();
        getActivity().setTitle(getFlexString(R.string.sr_app_name));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void onPause() {
        Logger.debug(this, "onPause");

        tripTableController.unsubscribe(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putBoolean(OUT_NAV_TO_LAST_TRIP, navigateToLastTrip);
    }

    @Override
    public void onDestroy() {
        if (!getActivity().isChangingConfigurations()) { // clear cache if fragment is not going to be recreated
            fragmentArgumentCache.remove(TripFragment.class);
        }
        super.onDestroy();
    }

    public final void tripMenu(final Trip trip) {
        if (!presenter.isExternalStorage()) {
            Toast.makeText(getActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
            return;
        }

        if (trip == null) {
            navigationHandler.navigateToCreateTripFragment();
        } else {
            navigationHandler.navigateToEditTripFragment(trip);
        }
    }

    public final boolean editTrip(final Trip trip) {
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        final String[] editTripItems = flex.getStringArray(getActivity(), R.array.EDIT_TRIP_ITEMS);
        builder.setTitle(trip.getName())
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setItems(editTripItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        final String selection = editTripItems[item];
                        if (selection == editTripItems[0]) {
                            TripFragment.this.tripMenu(trip);
                        } else if (selection == editTripItems[1]) {
                            TripFragment.this.deleteTrip(trip);
                        }
                        dialog.cancel();
                    }
                }).show();
        return true;
    }

    public final void deleteTrip(final Trip trip) {
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.delete_item, trip.getName()))
                .setMessage(getString(R.string.delete_sync_information))
                .setCancelable(true)
                .setPositiveButton(getFlexString(R.string.DIALOG_TRIP_DELETE_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        tripTableController.delete(trip, new DatabaseOperationMetadata());
                    }
                })
                .setNegativeButton(getFlexString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        viewReceipts(tripCardAdapter.getItem(position));
        // v.setSelected(true);
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

            if (!trips.isEmpty() && navigateToLastTrip) {
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

    private void viewReceipts(Trip trip) {
        navigationHandler.navigateToReportInfoFragment(trip);
    }

    public void showRatingTooltip() {
        tooltip.setQuestion(R.string.rating_tooltip_text, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analytics.record(Events.Ratings.UserDeclinedRatingPrompt);
                navigationHandler.showDialog(new FeedbackDialogFragment());
                tooltip.hideWithAnimation();
                presenter.dontShowRatingPrompt();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analytics.record(Events.Ratings.UserAcceptedRatingPrompt);
                navigationHandler.showDialog(new RatingDialogFragment());
                tooltip.hideWithAnimation();
                presenter.dontShowRatingPrompt();
            }
        });

        tooltip.showWithAnimation();
        analytics.record(Events.Ratings.RatingPromptShown);
    }

    private String getFlexString(int id){
        return getFlexString(flex, id);
    }
}