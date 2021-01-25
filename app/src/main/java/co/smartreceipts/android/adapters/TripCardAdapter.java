package co.smartreceipts.android.adapters;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.BackupProvidersManager;

public class TripCardAdapter extends CardAdapter<Trip> {

    private final DateFormatter dateFormatter;

	public TripCardAdapter(@NonNull Context context,
						   @NonNull UserPreferenceManager preferences,
						   @NonNull BackupProvidersManager backupProvidersManager,
                           @NonNull DateFormatter dateFormatter) {
		super(context, preferences, backupProvidersManager);
		this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
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
	protected void setDetailsTextView(TextView textView, Trip data) {
		final String start = dateFormatter.getFormattedDate(data.getStartDisplayableDate());
		final String end = dateFormatter.getFormattedDate(data.getEndDisplayableDate());
		textView.setText(getContext().getString(R.string.trip_adapter_list_item_to, start, end));
	}

}
