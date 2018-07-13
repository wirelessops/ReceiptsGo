package co.smartreceipts.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.TextView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.BackupProvidersManager;

public class TripCardAdapter extends CardAdapter<Trip> {

	public TripCardAdapter(@NonNull Context context, @NonNull UserPreferenceManager preferences, @NonNull BackupProvidersManager backupProvidersManager) {
		super(context, preferences, backupProvidersManager);
	}
	
	@Override
	protected String getPrice(Trip data) {
		return data.getPrice().getCurrencyFormattedPrice();
	}
	
	@Override
	protected void setPriceTextView(TextView textView, Trip data) {
		textView.setText(getPrice(data));
	}
	
	@Override
	protected void setNameTextView(TextView textView, Trip data) {
		textView.setText(data.getName());
	}
	
	@Override
	protected void setDateTextView(TextView textView, Trip data) {
		final String dateSeparator = getPreferences().get(UserPreference.General.DateSeparator);
		final String from = data.getFormattedStartDate(getContext(), dateSeparator);
		final String to = data.getFormattedEndDate(getContext(), dateSeparator);
		final String separator = getContext().getString(R.string.trip_adapter_list_item_to);
		// TODO: Use string placeholders
		textView.setText(from + separator + to);
	}

}
