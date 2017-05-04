package co.smartreceipts.android.adapters;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.widget.backups.AutomaticBackupsInfoDialogFragment;

public class ReceiptCardAdapter extends CardAdapter<Receipt> {

    private final NavigationHandler navigationHandler;

	public ReceiptCardAdapter(FragmentActivity activity, NavigationHandler navigationHandler,
							  UserPreferenceManager preferences, BackupProvidersManager backupProvidersManager) {
		super(activity, preferences, backupProvidersManager);
        this.navigationHandler = navigationHandler;
	}
	
	@Override
	protected String getPrice(Receipt data) {
		return data.getPrice().getCurrencyFormattedPrice();
	}
	
	@Override
	protected void setPriceTextView(TextView textView, Receipt data) {
		textView.setText(getPrice(data));
	}
	
	@Override
	protected void setNameTextView(TextView textView, Receipt data) {
        if (TextUtils.isEmpty(data.getName())) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(data.getName());
            textView.setVisibility(View.VISIBLE);
        }
	}
	
	@Override
	protected void setDateTextView(TextView textView, Receipt data) {
		if (getPreferences().get(UserPreference.Layout.IncludeReceiptDateInLayout)) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getFormattedDate(getContext(), getPreferences().get(UserPreference.General.DateSeparator)));
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void setCategory(TextView textView, Receipt data) {
		if (getPreferences().get(UserPreference.Layout.IncludeReceiptCategoryInLayout)) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getCategory().getName());
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void setMarker(TextView textView, Receipt data) {
		if (getPreferences().get(UserPreference.Layout.IncludeReceiptFileMarkerInLayout)) {
			textView.setVisibility(View.VISIBLE);
			textView.setText(data.getMarkerAsString(getContext()));
		}
		else {
			textView.setVisibility(View.GONE);
		}
	}

    @Override
    protected void setSyncStateImage(ImageView image, Receipt data) {
        if (mBackupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
            image.setClickable(false);
            if (data.getSyncState().isSynced(SyncProvider.GoogleDrive)) {
                Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mSyncedDrawable).into(image);
            } else {
                Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mNotSyncedDrawable).into(image);
            }
            image.setOnClickListener(null);
        } else {
            image.setClickable(true);
            Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mCloudDisabledDrawable).into(image);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigationHandler.showDialog(new AutomaticBackupsInfoDialogFragment());
                }
            });
        }
    }
}
