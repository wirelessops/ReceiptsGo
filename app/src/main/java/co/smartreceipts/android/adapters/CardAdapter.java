package co.smartreceipts.android.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
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

    protected final BackupProvidersManager backupProvidersManager;
    protected final Drawable cloudDisabledDrawable;
    protected final Drawable notSyncedDrawable;
    protected final Drawable syncedDrawable;

    private final LayoutInflater inflater;
    private final UserPreferenceManager preferences;
    private final Context context;
    private final float cardPriceTextSize;

    private List<T> data;

    private int listViewWidth, priceLayoutWidth;
    private int oldLongestPriceWidth, newLongestPriceWidth;

    private T selectedItem;

    public CardAdapter(@NonNull Context context, @NonNull UserPreferenceManager preferences, @NonNull BackupProvidersManager backupProvidersManager) {
        this(context, preferences, backupProvidersManager, Collections.<T>emptyList());
    }

    public CardAdapter(@NonNull Context context, @NonNull UserPreferenceManager preferences, @NonNull BackupProvidersManager backupProvidersManager, @NonNull List<T> data) {
        inflater = LayoutInflater.from(context);
        this.preferences = preferences;
        this.context = context;
        this.data = new ArrayList<>(data);
        this.backupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);

        cloudDisabledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_off_24dp, context.getTheme());
        notSyncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_queue_24dp, context.getTheme());
        syncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_done_24dp, context.getTheme());

        cardPriceTextSize = this.context.getResources().getDimension(getCardPriceTextSizeResource());
    }

    @Override
    public int getCount() {
        if (data == null) {
            return 0;
        } else {
            return data.size();
        }
    }

    @Override
    public T getItem(int i) {
        if (data == null) {
            return null;
        } else {
            return data.get(i);
        }
    }

    public long getItemId(int i) {
        return i;
    }

    public final Context getContext() {
        return context;
    }

    public final UserPreferenceManager getPreferences() {
        return preferences;
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

        if (listViewWidth <= 0 || listViewWidth != parent.getWidth() || oldLongestPriceWidth != newLongestPriceWidth) {
            listViewWidth = parent.getWidth();

            oldLongestPriceWidth = newLongestPriceWidth;
            int maxPriceLayoutWidth = (listViewWidth / MAX_PRICE_WIDTH_DIVIDER); // Set to half width
            int minPriceLayoutWidth = (listViewWidth / MIN_PRICE_WIDTH_DIVIDER); // Set to 1/6 width

            priceLayoutWidth = newLongestPriceWidth;
            if (newLongestPriceWidth < minPriceLayoutWidth) {
                priceLayoutWidth = minPriceLayoutWidth;
            } else if (newLongestPriceWidth > maxPriceLayoutWidth) {
                priceLayoutWidth = maxPriceLayoutWidth;
            }
        }


        MyViewHolder holder;
        final T data = getItem(i);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.simple_card, parent, false);
            holder = new MyViewHolder();
            holder.price = convertView.findViewById(R.id.price);
            holder.name = convertView.findViewById(android.R.id.title);
            holder.date = convertView.findViewById(android.R.id.summary);
            holder.category = convertView.findViewById(android.R.id.text1);
            holder.marker = convertView.findViewById(android.R.id.text2);
            holder.syncState = convertView.findViewById(R.id.card_sync_state);
            convertView.setTag(holder);
        } else {
            holder = (MyViewHolder) convertView.getTag();
        }


        if (holder.price.getLayoutParams().width != priceLayoutWidth) {
            holder.price.getLayoutParams().width = priceLayoutWidth;
            holder.price.requestLayout();
        }
        setPriceTextView(holder.price, data);
        setNameTextView(holder.name, data);
        setDateTextView(holder.date, data);
        setCategory(holder.category, data);
        setMarker(holder.marker, data);
        setSyncStateImage(holder.syncState, data);

        if (selectedItem != null && this.data.indexOf(selectedItem) == i) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_card_background));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
        }

        return convertView;
    }

    public void setSelectedItem(@Nullable T item) {
        selectedItem = item;
        notifyDataSetChanged();
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
        if (backupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
            if (data instanceof Syncable) {
                final Syncable syncableData = (Syncable) data;
                if (backupProvidersManager.getLastDatabaseSyncTime().getTime() >= syncableData.getSyncState().getLastLocalModificationTime().getTime()
                        && syncableData.getSyncState().getLastLocalModificationTime().getTime() >= 0) {
                    Picasso.get().load(Uri.EMPTY).placeholder(syncedDrawable).into(image);
                } else {
                    Picasso.get().load(Uri.EMPTY).placeholder(notSyncedDrawable).into(image);
                }
            } else {
                image.setVisibility(View.GONE);
            }
        } else {
            Picasso.get().load(Uri.EMPTY).placeholder(cloudDisabledDrawable).into(image);
        }
    }

    protected int getCardPriceTextSizeResource() {
        return R.dimen.card_price_size;
    }

    public final synchronized void notifyDataSetChanged(List<T> newData) {
        data = new ArrayList<>(newData);
        calculateLongestPriceWidth();
        super.notifyDataSetChanged();
    }

    private void calculateLongestPriceWidth() {
        if (data != null && data.size() != 0) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextSize(cardPriceTextSize * PRICE_WIDTH_BUFFER);
            paint.setTypeface(Typeface.DEFAULT_BOLD); // Set in the Price field
            int curr = 0, measured = 0;
            final int size = data.size();
            for (int i = 0; i < size; i++) {
                measured = (int) paint.measureText(getPrice(data.get(i)));
                if (measured > curr) {
                    curr = measured;
                }
            }
            newLongestPriceWidth = curr;
        }
    }

}