package co.smartreceipts.android.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.model.Syncable;
import co.smartreceipts.android.sync.provider.SyncProvider;

public class CardAdapter<T> extends BaseAdapter {
	
	private static final int MAX_PRICE_WIDTH_DIVIDER = 2;
	private static final int MIN_PRICE_WIDTH_DIVIDER = 6;
	private static final float PRICE_WIDTH_BUFFER = 1.1f;

    protected final BackupProvidersManager mBackupProvidersManager;
    protected final Drawable mCloudDisabledDrawable;
    protected final Drawable mNotSyncedDrawable;
    protected final Drawable mSyncedDrawable;

	private final LayoutInflater mInflater;
	private final UserPreferenceManager mPreferences;
	private final Context mContext;
	private final float mCardPriceTextSize;

    private List<T> mData;
    private int mMinPriceWidth, mMaxPriceWidth, mCurrentPriceWidth;
	
	public CardAdapter(@NonNull Context context, @NonNull UserPreferenceManager preferences, @NonNull BackupProvidersManager backupProvidersManager) {
		this(context, preferences, backupProvidersManager, Collections.<T>emptyList());
	}

    public CardAdapter(@NonNull Context context, @NonNull UserPreferenceManager preferences, @NonNull BackupProvidersManager backupProvidersManager, @NonNull List<T> data) {
        mInflater = LayoutInflater.from(context);
        mPreferences = preferences;
        mContext = context;
        mData = new ArrayList<>(data);
        mBackupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        mCloudDisabledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_off_24dp, context.getTheme());
        mNotSyncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_queue_24dp, context.getTheme());
        mSyncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_done_24dp, context.getTheme());
        final Resources resources = mContext.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        mMaxPriceWidth = (metrics.widthPixels / MAX_PRICE_WIDTH_DIVIDER); // Set to half width
        mMinPriceWidth = (metrics.widthPixels / MIN_PRICE_WIDTH_DIVIDER); // Set to 1/6 width
        mCurrentPriceWidth = mMinPriceWidth;
        mCardPriceTextSize = resources.getDimension(getCardPriceTextSizeResouce());
    }

	@Override
	public int getCount() {
		if (mData == null) {
			return 0;
		}
		else {
			return mData.size();
		}
	}

	@Override
	public T getItem(int i) {
		if (mData == null) {
			return null;
		}
		else {
			return mData.get(i);
		}
	}
	
	public long getItemId(int i) {
		return i;
	}
	
	public final Context getContext() {
		return mContext;
	}
	
	public final UserPreferenceManager getPreferences() {
		return mPreferences;
	}
	
	private static class MyViewHolder {
		public TextView price;
		public TextView name;
		public TextView date;
		public TextView category;
		public TextView marker;
        public ImageView syncState;
	}

	@Override
	public View getView(final int i, View convertView, ViewGroup parent) {
		MyViewHolder holder;
		final T data = getItem(i);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.simple_card, parent, false);
			holder = new MyViewHolder();
			holder.price = (TextView) convertView.findViewById(R.id.price);
			holder.name = (TextView) convertView.findViewById(android.R.id.title);
			holder.date = (TextView) convertView.findViewById(android.R.id.summary);
			holder.category = (TextView) convertView.findViewById(android.R.id.text1);
			holder.marker = (TextView) convertView.findViewById(android.R.id.text2);
            holder.syncState = (ImageView) convertView.findViewById(R.id.card_sync_state);
			convertView.setTag(holder);
		}
		else {
			holder = (MyViewHolder) convertView.getTag();
		}
		if (holder.price.getLayoutParams().width != mCurrentPriceWidth) {
			holder.price.getLayoutParams().width = mCurrentPriceWidth;
			holder.price.requestLayout();
		}
		setPriceTextView(holder.price, data);
		setNameTextView(holder.name, data);
		setDateTextView(holder.date, data);
		setCategory(holder.category, data);
		setMarker(holder.marker, data);
        setSyncStateImage(holder.syncState, data);
		return convertView;
	}
	
	protected int getPriceLayoutWidth() {
		if (mData == null) {
			return mMaxPriceWidth;
		}
		else {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setTextSize(mCardPriceTextSize*PRICE_WIDTH_BUFFER);
			paint.setTypeface(Typeface.DEFAULT_BOLD); // Set in the Price field
			int curr = mMinPriceWidth, measured;
			final int size = mData.size();
			for (int i=0; i < size; i++) {
				measured = (int) paint.measureText(getPrice(mData.get(i)));
				if (measured > curr) {
					curr = measured;
				}
			}
			if (curr < mMaxPriceWidth) {
				mCurrentPriceWidth = curr;
			}
			else {
				mCurrentPriceWidth = mMaxPriceWidth;
			}
			return mMaxPriceWidth;
		}
	}
	
	protected String getPrice(T data) {
		return "";
	}
	
	protected void setPriceTextView(TextView textView, T data) {
		
	}
	
	protected void setNameTextView(TextView textView, T data) {
		
	}
	
	protected void setDateTextView(TextView textView, T data) {
		
	}
	
	protected void setCategory(TextView textView, T data) {
		textView.setVisibility(View.GONE);
	}
	
	protected void setMarker(TextView textView, T data) {
		textView.setVisibility(View.GONE);
	}

    protected void setSyncStateImage(ImageView image, T data) {
        image.setClickable(false);
        if (mBackupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
            if (data instanceof Syncable) {
                final Syncable syncableData = (Syncable) data;
                if (mBackupProvidersManager.getLastDatabaseSyncTime().getTime() >= syncableData.getSyncState().getLastLocalModificationTime().getTime()
                        && syncableData.getSyncState().getLastLocalModificationTime().getTime() >= 0) {
                    Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mSyncedDrawable).into(image);
                } else {
                    Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mNotSyncedDrawable).into(image);
                }
            } else {
                image.setVisibility(View.GONE);
            }
        } else {
            Picasso.with(getContext()).load(Uri.EMPTY).placeholder(mCloudDisabledDrawable).into(image);
        }
    }
	
	protected int getCardPriceTextSizeResouce() {
		return R.dimen.card_price_size;
	}
	
	public final synchronized void notifyDataSetChanged(List<T> newData) {
		mData = new ArrayList<>(newData);
		getPriceLayoutWidth();
		super.notifyDataSetChanged();
	}

}