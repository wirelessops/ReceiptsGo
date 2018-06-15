package co.smartreceipts.android.distance.editor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.currency.widget.CurrencyListEditorPresenter;
import co.smartreceipts.android.currency.widget.DefaultCurrencyListEditorView;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.distance.editor.currency.DistanceCurrencyCodeSupplier;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import dagger.android.support.AndroidSupportInjection;
import wb.android.autocomplete.AutoCompleteAdapter;

public class DistanceDialogFragment extends DialogFragment implements OnClickListener {

    public static final String TAG = DistanceDialogFragment.class.getSimpleName();
    private static final String ARG_SUGGESTED_DATE = "arg_suggested_date";

    @Inject
    DatabaseHelper database;

    @Inject
    Analytics analytics;

    @Inject
    DistanceTableController distanceTableController;

    @Inject
    UserPreferenceManager userPreferenceManager;

    // Butterknife Fields
    @BindView(R.id.dialog_mileage_distance)
    EditText distanceEditText;

    @BindView(R.id.dialog_mileage_rate)
    EditText rateEditText;

    @BindView(R.id.dialog_mileage_location)
    AutoCompleteTextView locationAutoCompleteTextView;

    @BindView(R.id.dialog_mileage_comment)
    EditText commentEditText;

    @BindView(R.id.dialog_mileage_date)
    DateEditText dateEditText;

    @BindView(R.id.dialog_mileage_currency)
    Spinner currencySpinner;

    // View cleanup
    private Unbinder unbinder;

    // State member variables
    private Trip trip;
    private Distance updateableDistance;
    private Date suggestedDate;

    private AutoCompleteAdapter locationAutoCompleteAdapter;

    // Presenters
    private CurrencyListEditorPresenter currencyListEditorPresenter;

    /**
     * Creates a new instance of a {@link DistanceDialogFragment}, which
     * can be used to enter a new distance item
     *
     * @param trip - the parent {@link Trip}
     * @return - a {@link DistanceDialogFragment}
     */
    public static DistanceDialogFragment newInstance(@NonNull Trip trip) {
        return newInstance(trip, null, null);
    }


    /**
     * Creates a new instance of a {@link DistanceDialogFragment}, which
     * can be used to enter a new distance item
     *
     * @param trip          - the parent {@link Trip}
     * @param suggestedDate - the suggested {@link Date} to display to the user when creating a new distance item
     * @return - a {@link DistanceDialogFragment}
     */
    public static DistanceDialogFragment newInstance(@NonNull Trip trip, @Nullable Date suggestedDate) {
        return newInstance(trip, null, suggestedDate);
    }


    /**
     * Creates a new instance of a {@link DistanceDialogFragment}, which
     * can be used to update an existing distance item
     *
     * @param trip     - the parent {@link Trip}
     * @param distance - the {@link Distance} object to update
     * @return - a {@link DistanceDialogFragment}
     */
    public static DistanceDialogFragment newInstance(@NonNull Trip trip, @Nullable Distance distance) {
        return newInstance(trip, distance, null);
    }


    private static DistanceDialogFragment newInstance(@NonNull Trip trip, @Nullable Distance distance, @Nullable Date suggestedDate) {
        final DistanceDialogFragment dialog = new DistanceDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        if (distance != null) {
            args.putParcelable(Distance.PARCEL_KEY, distance);
        }
        if (suggestedDate != null) {
            args.putLong(ARG_SUGGESTED_DATE, suggestedDate.getTime() + 1);
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trip = getArguments().getParcelable(Trip.PARCEL_KEY);
        Preconditions.checkNotNull(trip, "A valid trip is required");

        updateableDistance = getArguments().getParcelable(Distance.PARCEL_KEY);

        // Default to "now" if not suggested date was set
        final Time now = new Time();
        now.setToNow();
        suggestedDate = new Date(getArguments().getLong(ARG_SUGGESTED_DATE, now.toMillis(false)));

        final DistanceCurrencyCodeSupplier currencyCodeSupplier = new DistanceCurrencyCodeSupplier(trip, updateableDistance);
        final DefaultCurrencyListEditorView defaultCurrencyListEditorView = new DefaultCurrencyListEditorView(getContext(), () -> currencySpinner);
        currencyListEditorPresenter = new CurrencyListEditorPresenter(defaultCurrencyListEditorView, database, currencyCodeSupplier, savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View rootView = inflater.inflate(R.layout.dialog_mileage, null);
        this.unbinder = ButterKnife.bind(this, rootView);

        dateEditText.setFocusable(false);
        dateEditText.setFocusableInTouchMode(false);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        if (updateableDistance == null) {
            // New Distance
            builder.setTitle(getString(R.string.dialog_mileage_title_create));
            builder.setPositiveButton(getString(R.string.dialog_mileage_positive_create), this);
            dateEditText.setDate(suggestedDate);
            dateEditText.setDateSeparator(userPreferenceManager.get(UserPreference.General.DateSeparator));
            final float distanceRate = userPreferenceManager.get(UserPreference.Distance.DefaultDistanceRate);
            if (distanceRate > 0) {
                rateEditText.setText(ModelUtils.getDecimalFormattedValue(BigDecimal.valueOf(distanceRate), Distance.RATE_PRECISION));
            }
            if (locationAutoCompleteAdapter == null) {
                locationAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(),
                        DatabaseHelper.TAG_DISTANCE_LOCATION, database);
            } else {
                locationAutoCompleteAdapter.reset();
            }
            locationAutoCompleteTextView.setAdapter(locationAutoCompleteAdapter);
            distanceEditText.setOnFocusChangeListener((view, hasFocus) -> SoftKeyboardManager.showKeyboard(view));
        } else {
            // Update distance
            builder.setTitle(getString(R.string.dialog_mileage_title_update));
            builder.setPositiveButton(getString(R.string.dialog_mileage_positive_update), this);
            builder.setNeutralButton(getString(R.string.dialog_mileage_neutral_delete), this);
            distanceEditText.setText(updateableDistance.getDecimalFormattedDistance());
            rateEditText.setText(updateableDistance.getDecimalFormattedRate());
            locationAutoCompleteTextView.setText(updateableDistance.getLocation());
            commentEditText.setText(updateableDistance.getComment());
            dateEditText.setDate(updateableDistance.getDate());
            dateEditText.setTimeZone(updateableDistance.getTimeZone());
            dateEditText.setDateSeparator(userPreferenceManager.get(UserPreference.General.DateSeparator));
        }
        builder.setNegativeButton(android.R.string.cancel, this);

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        currencyListEditorPresenter.subscribe();
    }

    @Override
    public void onPause() {
        currencyListEditorPresenter.unsubscribe();
        if (locationAutoCompleteAdapter != null) {
            locationAutoCompleteAdapter.onPause();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        currencyListEditorPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        this.unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Create/Edit
            final String currency = this.currencySpinner.getSelectedItem().toString();
            final String location = this.locationAutoCompleteTextView.getText().toString();
            final String comment = this.commentEditText.getText().toString();
            final Date date = this.dateEditText.getDate();
            final TimeZone timeZone = this.dateEditText.getTimeZone();

            if (updateableDistance == null) {
                // We're inserting a new one
                final BigDecimal distance = getBigDecimalFromString(this.distanceEditText.getText().toString(), BigDecimal.ZERO);
                final BigDecimal rate = getBigDecimalFromString(this.rateEditText.getText().toString(), BigDecimal.ZERO);
                final DistanceBuilderFactory builder = new DistanceBuilderFactory();
                builder.setTrip(trip);
                builder.setLocation(location);
                builder.setDistance(distance);
                builder.setDate(date);
                builder.setTimezone(timeZone);
                builder.setRate(rate);
                builder.setCurrency(currency);
                builder.setComment(comment);
                analytics.record(Events.Distance.PersistNewDistance);
                distanceTableController.insert(builder.build(), new DatabaseOperationMetadata());
            } else {
                // We're updating
                final BigDecimal distance = getBigDecimalFromString(this.distanceEditText.getText().toString(), updateableDistance.getDistance());
                final BigDecimal rate = getBigDecimalFromString(this.rateEditText.getText().toString(), updateableDistance.getRate());
                final DistanceBuilderFactory builder = new DistanceBuilderFactory(updateableDistance);
                builder.setLocation(location);
                builder.setDistance(distance);
                builder.setDate(date);
                builder.setTimezone(timeZone);
                builder.setRate(rate);
                builder.setCurrency(currency);
                builder.setComment(comment);
                analytics.record(Events.Distance.PersistUpdateDistance);
                distanceTableController.update(updateableDistance, builder.build(), new DatabaseOperationMetadata());
            }
        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
            // Delete
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.delete_item, updateableDistance.getLocation()));
            builder.setMessage(R.string.delete_sync_information);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.delete, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    distanceTableController.delete(updateableDistance, new DatabaseOperationMetadata());
                    dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });
            builder.show();
        }
        dismiss();
    }

    /**
     * @param number   - a string containing a number
     * @param fallback - the {@link BigDecimal} to return if the string is NaN
     * @return a {@link BigDecimal} or the fallback param if not
     */
    private BigDecimal getBigDecimalFromString(String number, BigDecimal fallback) {
        if (TextUtils.isEmpty(number)) {
            return fallback;
        }
        try {
            return new BigDecimal(number.replace(",", "."));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
