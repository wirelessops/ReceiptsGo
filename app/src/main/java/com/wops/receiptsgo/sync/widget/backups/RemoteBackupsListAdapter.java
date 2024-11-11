package com.wops.receiptsgo.sync.widget.backups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.model.utils.ModelUtils;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.receiptsgo.sync.network.NetworkManager;
import com.wops.receiptsgo.sync.network.SupportedNetworkType;
import co.smartreceipts.core.sync.model.RemoteBackupMetadata;

public class RemoteBackupsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final View headerView;
    private final NavigationHandler navigationHandler;
    private final BackupProvidersManager backupProvidersManager;
    private final UserPreferenceManager preferences;
    private final NetworkManager networkManager;
    private final List<RemoteBackupMetadata> backupMetadataList;

    public RemoteBackupsListAdapter(@NonNull View headerView, @NonNull NavigationHandler navigationHandler,
                                    @NonNull BackupProvidersManager backupProvidersManager, @NonNull UserPreferenceManager preferences,
                                    @NonNull NetworkManager networkManager) {
        this(headerView, navigationHandler, backupProvidersManager, preferences,
                networkManager, Collections.emptyList());
    }

    public RemoteBackupsListAdapter(@NonNull View headerView, @NonNull NavigationHandler navigationHandler,
                                    @NonNull BackupProvidersManager backupProvidersManager, @NonNull UserPreferenceManager preferences,
                                    @NonNull NetworkManager networkManager, @NonNull List<RemoteBackupMetadata> backupMetadataList) {
        this.headerView = Preconditions.checkNotNull(headerView);
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.backupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        this.preferences = Preconditions.checkNotNull(preferences);
        this.networkManager = Preconditions.checkNotNull(networkManager);
        this.backupMetadataList = new ArrayList<>(backupMetadataList);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(headerView);
        } else {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote_backup, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;
            final Context context = itemHolder.backupDeviceNameTextView.getContext();
            final RemoteBackupMetadata metadata = backupMetadataList.get(position - 1);
            if (metadata.getSyncDeviceId().equals(backupProvidersManager.getDeviceSyncId())) {
                itemHolder.backupDeviceNameTextView.setText(context.getString(R.string.existing_remote_backup_current_device, metadata.getSyncDeviceName()));
            } else {
                itemHolder.backupDeviceNameTextView.setText(metadata.getSyncDeviceName());
            }
            itemHolder.backupProviderTextView.setText(R.string.auto_backup_source_google_drive);
            itemHolder.backupDateTextView.setText(ModelUtils.getFormattedDate(metadata.getLastModifiedDate(), TimeZone.getDefault(), context, preferences.get(UserPreference.General.DateSeparator)));
            final View.OnClickListener onClickListener = view -> {
                final PopupMenu popupMenu = new PopupMenu(context, itemHolder.backupDeviceNameTextView);
                popupMenu.getMenuInflater().inflate(R.menu.remote_backups_list_item_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (!networkManager.isNetworkAvailable() && networkManager.getSupportedNetworkType() == SupportedNetworkType.WifiOnly) {
                        Toast.makeText(headerView.getContext(), headerView.getContext().getString(R.string.error_no_wifi), Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        if (item.getItemId() == R.id.remote_backups_list_item_menu_restore) {
                            navigationHandler.showDialog(ImportRemoteBackupDialogFragment.newInstance(metadata));
                            return true;
                        } else if (item.getItemId() == R.id.remote_backups_list_item_menu_rename) {
                            navigationHandler.showDialog(RenameRemoteBackupDialogFragment.newInstance(metadata));
                            return true;
                        } else if (item.getItemId() == R.id.remote_backups_list_item_menu_delete) {
                            navigationHandler.showDialog(DeleteRemoteBackupDialogFragment.newInstance(metadata));
                            return true;
                        } else if (item.getItemId() == R.id.remote_backups_list_item_menu_download_images) {
                            navigationHandler.showDialog(DownloadRemoteBackupImagesProgressDialogFragment.newInstance(metadata));
                            return true;
                        } else if (item.getItemId() == R.id.remote_backups_list_item_menu_download_images_debug) {
                            navigationHandler.showDialog(DownloadRemoteBackupImagesProgressDialogFragment.newInstance(metadata, true));
                            return true;
                        } else {
                            throw new IllegalArgumentException("Unsupported menu type was selected");
                        }
                    }
                });
                popupMenu.show();
            };
            itemHolder.parentView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return backupMetadataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        final View headerView;

        HeaderViewHolder(@NonNull View view) {
            super(view);
            headerView = view;
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        final View parentView;
        final TextView backupDeviceNameTextView;
        final TextView backupProviderTextView;
        final TextView backupDateTextView;

        ItemViewHolder(@NonNull View view) {
            super(view);
            parentView = view;
            backupDeviceNameTextView = view.findViewById(R.id.remote_backup_device_name);
            backupProviderTextView = view.findViewById(R.id.remote_backup_provider);
            backupDateTextView = view.findViewById(R.id.remote_backup_date);
        }
    }
}
