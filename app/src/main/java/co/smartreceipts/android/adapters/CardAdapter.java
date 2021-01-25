package co.smartreceipts.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.common.base.Preconditions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.core.sync.model.Syncable;
import co.smartreceipts.core.sync.provider.SyncProvider;

public class CardAdapter<T> extends BaseAdapter {

    protected final BackupProvidersManager backupProvidersManager;
    protected final Drawable cloudDisabledDrawable;
    protected final Drawable notSyncedDrawable;
    protected final Drawable syncedDrawable;

    private final LayoutInflater inflater;
    private final UserPreferenceManager preferences;
    private final Context context;

    private List<T> data;

    private T selectedItem;

    public CardAdapter(@NonNull Context context, @NonNull UserPreferenceManager preferences, @NonNull BackupProvidersManager backupProvidersManager) {
        this(context, preferences, backupProvidersManager, Collections.emptyList());
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
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public T getItem(int i) {
        return data == null ? null : data.get(i);
    }

    @NonNull
    public ArrayList<T> getData() {
        return data == null ? new ArrayList<>() : new ArrayList<>(data);
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

    private static class TripDistanceViewHolder {
        public TextView price;
        public TextView name;
        public TextView details;
        public ImageView syncState;
        public ImageView selectionMarker;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup parent) {

        TripDistanceViewHolder holder;
        final T data = getItem(i);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_trip_or_distance_card, parent, false);
            holder = new TripDistanceViewHolder();
            holder.price = convertView.findViewById(R.id.text_price);
            holder.name = convertView.findViewById(R.id.text_name);
            holder.details = convertView.findViewById(R.id.text_details);
            holder.selectionMarker = convertView.findViewById(R.id.image_selection_marker);
            holder.syncState = convertView.findViewById(R.id.image_sync_state);
            convertView.setTag(holder);
        } else {
            holder = (TripDistanceViewHolder) convertView.getTag();
        }


        setPriceTextView(holder.price, data);
        setNameTextView(holder.name, data);
        setDetailsTextView(holder.details, data);
        setSyncStateImage(holder.syncState, data);

        if (selectedItem != null && this.data.indexOf(selectedItem) == i) {
            convertView.setSelected(true);
            showItemSelection(holder, true);
        } else {
            convertView.setSelected(false);
            showItemSelection(holder, false);
        }

        return convertView;
    }

    private void showItemSelection(TripDistanceViewHolder holder, boolean isSelected) {
        final int colorSelected = context.getResources().getColor(R.color.smart_receipts_colorPrimary);
        final int colorDefault = context.getResources().getColor(R.color.text_primary_color);

        if (isSelected) {
            holder.selectionMarker.setVisibility(View.VISIBLE);
            holder.name.setTextColor(colorSelected);
            holder.price.setTextColor(colorSelected);
        } else {
            holder.selectionMarker.setVisibility(View.GONE);
            holder.name.setTextColor(colorDefault);
            holder.price.setTextColor(colorDefault);
        }
    }

    public void setSelectedItem(@Nullable T item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    protected String getPrice(T data) {
        return "";
    }

    protected void setPriceTextView(TextView textView, T data) { }

    protected void setNameTextView(TextView textView, T data) { }

    protected void setDetailsTextView(TextView textView, T data) { }

    protected void setSyncStateImage(ImageView image, T data) {
        image.setClickable(false);
        if (data instanceof Syncable) {
            final Syncable syncableData = (Syncable) data;
            if (backupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
                if (backupProvidersManager.getLastDatabaseSyncTime().getTime() >= syncableData.getSyncState().getLastLocalModificationTime().getTime()
                        && syncableData.getSyncState().getLastLocalModificationTime().getTime() >= 0) {
                    Picasso.get().load(Uri.EMPTY).placeholder(syncedDrawable).into(image);
                } else {
                    Picasso.get().load(Uri.EMPTY).placeholder(notSyncedDrawable).into(image);
                }
            } else {
                if (backupProvidersManager.getLastDatabaseSyncTime().getTime() < syncableData.getSyncState().getLastLocalModificationTime().getTime()) {
                    Picasso.get().load(Uri.EMPTY).placeholder(cloudDisabledDrawable).into(image);
                } else if (backupProvidersManager.getLastDatabaseSyncTime().getTime() >= syncableData.getSyncState().getLastLocalModificationTime().getTime()) {
                    Picasso.get().load(Uri.EMPTY).placeholder(syncedDrawable).into(image);
                }
            }
        } else {
            image.setVisibility(View.GONE);
        }
    }

    public final synchronized void notifyDataSetChanged(List<T> newData) {
        data = new ArrayList<>(newData);
        super.notifyDataSetChanged();
    }

}