package co.smartreceipts.android.receipts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableCardsAdapter;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.widget.backups.AutomaticBackupsInfoDialogFragment;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ReceiptsAdapter extends DraggableCardsAdapter<Receipt> {

    private final UserPreferenceManager preferences;
    private final BackupProvidersManager backupProvidersManager;
    private final NavigationHandler navigationHandler;
    private final Context context;
    private final TableController<Receipt> tableController;
    private final OrderingPreferencesManager orderingPreferencesManager;

    private Integer draggedReceiptNewPosition = null;

    private final PublishSubject<Receipt> itemClickSubject = PublishSubject.create();
    private final PublishSubject<Receipt> menuClickSubject = PublishSubject.create();
    private final PublishSubject<Receipt> imageClickSubject = PublishSubject.create();


    public ReceiptsAdapter(Context context, TableController<Receipt> tableController, UserPreferenceManager preferenceManager,
                           BackupProvidersManager backupProvidersManager, NavigationHandler navigationHandler,
                           OrderingPreferencesManager orderingPreferencesManager) {
        super();
        this.preferences = Preconditions.checkNotNull(preferenceManager);
        this.context = Preconditions.checkNotNull(context);
        this.backupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.tableController = Preconditions.checkNotNull(tableController);
        this.orderingPreferencesManager = Preconditions.checkNotNull(orderingPreferencesManager);
    }

    @Override
    public AbstractDraggableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receipt_card, parent, false);
        return new ReceiptViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(AbstractDraggableItemViewHolder holder, int position) {
        ReceiptViewHolder receiptHolder = (ReceiptViewHolder) holder;
        Receipt receipt = items.get(position);

        receiptHolder.itemView.setOnClickListener(v -> itemClickSubject.onNext(receipt));
        receiptHolder.menuButton.setOnClickListener(v -> menuClickSubject.onNext(receipt));
        receiptHolder.image.setOnClickListener(v -> imageClickSubject.onNext(receipt));

        if (receipt.hasPDF()) {
            setIcon(receiptHolder.image, R.drawable.ic_file_black_24dp);
        } else if (receipt.hasImage()) {
            receiptHolder.image.setPadding(0, 0, 0, 0);
            Picasso.with(context)
                    .load(receipt.getImage())
                    .fit()
                    .centerCrop()
                    .into(receiptHolder.image);
        } else {
            setIcon(receiptHolder.image, R.drawable.ic_receipt_white_24dp);
        }

        receiptHolder.price.setText(receipt.getPrice().getCurrencyFormattedPrice());
        receiptHolder.name.setText(receipt.getName());

        if (preferences.get(UserPreference.Layout.IncludeReceiptDateInLayout)) {
            receiptHolder.date.setVisibility(View.VISIBLE);
            receiptHolder.date.setText(receipt.getFormattedDate(context, preferences.get(UserPreference.General.DateSeparator)));
        } else {
            receiptHolder.date.setVisibility(View.GONE);
        }

        if (preferences.get(UserPreference.Layout.IncludeReceiptCategoryInLayout)) {
            receiptHolder.category.setVisibility(View.VISIBLE);
            receiptHolder.category.setText(receipt.getCategory().getName());
        } else {
            receiptHolder.category.setVisibility(View.GONE);
        }

        if (preferences.get(UserPreference.Layout.IncludeReceiptFileMarkerInLayout)) {
            receiptHolder.marker.setVisibility(View.VISIBLE);
            receiptHolder.marker.setText(receipt.getMarkerAsString(context));
        } else {
            receiptHolder.marker.setVisibility(View.GONE);
        }

        Drawable cloudDisabledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_off_24dp, context.getTheme());
        Drawable notSyncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_queue_24dp, context.getTheme());
        Drawable syncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_done_24dp, context.getTheme());

        if (backupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
            receiptHolder.syncState.setClickable(false);
            receiptHolder.syncState.setImageDrawable(receipt.getSyncState().isSynced(SyncProvider.GoogleDrive) ? syncedDrawable : notSyncedDrawable);
            receiptHolder.syncState.setOnClickListener(null);
        } else {
            receiptHolder.syncState.setClickable(true);
            receiptHolder.syncState.setImageDrawable(cloudDisabledDrawable);
            receiptHolder.syncState.setOnClickListener(view -> navigationHandler.showDialog(new AutomaticBackupsInfoDialogFragment()));
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) return;

        draggedReceiptNewPosition = toPosition;

        saveOrderIdsAsDateIfNeeded();
        updateDraggedItem(fromPosition, toPosition);

        super.onMoveItem(fromPosition, toPosition);
        saveNewOrder(tableController);
    }

    @Override
    public boolean onCheckCanStartDrag(AbstractDraggableItemViewHolder holder, int position, int x, int y) {
        return items.size() > 1;
    }

    private void updateDraggedItem(int oldPosition, int newPosition) {
        Logger.debug(this, "Reordering, from position " + oldPosition + " to position " + newPosition);

        // update custom order id for dragged item
        long newCustomOrderId;
        if (oldPosition < newPosition) { // move down
            newCustomOrderId = items.get(newPosition).getCustomOrderId() - 1;
        } else { // move up
            newCustomOrderId = items.get(newPosition).getCustomOrderId() + 1;
        }

        // updating local list
        final Receipt draggedReceipt = new ReceiptBuilderFactory(items.get(oldPosition)).setCustomOrderId(newCustomOrderId).build();
        items.remove(oldPosition);
        items.add(oldPosition, draggedReceipt);
    }

    @Override
    public void saveNewOrder(TableController<Receipt> tableController) {

        if (draggedReceiptNewPosition != null) {
            final Receipt draggedReceipt = items.get(draggedReceiptNewPosition);
            tableController.update(draggedReceipt, draggedReceipt, new DatabaseOperationMetadata());
            draggedReceiptNewPosition = null;
        }
    }

    private void saveOrderIdsAsDateIfNeeded() {
        // saving order id's like a time for all receipts at first before any order changes
        if (!orderingPreferencesManager.isReceiptsTableOrdered()) {

            List<Receipt> updatedItems = new ArrayList<>();

            for (Receipt item : items) {
                final Receipt updatedReceipt = new ReceiptBuilderFactory(item)
                        .setCustomOrderId(item.getDate().getTime() + item.getIndex()) // hack to prevent mixing items with same date
                        .build();
                tableController.update(item, updatedReceipt,
                        new DatabaseOperationMetadata());

                updatedItems.add(updatedReceipt);
            }

            items.clear();
            items.addAll(updatedItems);

            orderingPreferencesManager.saveReceiptsTableOrdering();
        }
    }

    Observable<Receipt> getItemClicks() {
        return itemClickSubject;
    }

    Observable<Receipt> getMenuClicks() {
        return menuClickSubject;
    }

    Observable<Receipt> getImageClicks() {
        return imageClickSubject;
    }

    private void setIcon(ImageView view, @DrawableRes int drawableRes) {
        final Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), drawableRes, context.getTheme());

        DrawableCompat.setTint(drawable, ResourcesCompat.getColor(context.getResources(), R.color.grey_image_tint, null));
        final int pixelPadding = context.getResources().getDimensionPixelOffset(R.dimen.card_image_padding);

        view.setImageDrawable(drawable);
        view.setPadding(pixelPadding, pixelPadding, pixelPadding, pixelPadding);
    }

    private static class ReceiptViewHolder extends AbstractDraggableItemViewHolder {

        public TextView price;
        public TextView name;
        public TextView date;
        public TextView category;
        public ImageView syncState;
        public ImageView image;
        ImageView menuButton;
        TextView marker;

        ReceiptViewHolder(View itemView) {
            super(itemView);

            price = itemView.findViewById(R.id.price);
            name = itemView.findViewById(android.R.id.title);
            date = itemView.findViewById(R.id.card_date);
            category = itemView.findViewById(android.R.id.text1);
            marker = itemView.findViewById(android.R.id.text2);
            syncState = itemView.findViewById(R.id.card_sync_state);
            menuButton = itemView.findViewById(R.id.card_menu);
            image = itemView.findViewById(R.id.card_image);
        }
    }
}
