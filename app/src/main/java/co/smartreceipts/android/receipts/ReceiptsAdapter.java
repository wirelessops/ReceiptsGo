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

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

public class ReceiptsAdapter extends DraggableCardsAdapter<Receipt> implements ReceiptsHeaderItemDecoration.StickyHeaderInterface {

    /**
     * List that contains all Receipts from items and needed Headers
     */
    private List<ReceiptsListItem> listItems;

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

        listItems = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).getListItemType();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public AbstractDraggableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        return viewType == ReceiptsListItem.TYPE_HEADER ? new ReceiptHeaderReceiptsListViewHolder(inflatedView)
                : new ReceiptReceiptsListViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(AbstractDraggableItemViewHolder holder, int position) {
        ((ReceiptsListViewHolder) holder).bindType(listItems.get(position));
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == ReceiptsListItem.TYPE_RECEIPT) {
            return ((ReceiptContentItem) listItems.get(position)).getReceipt().getId();
        } else {
            return ((ReceiptHeaderItem)listItems.get(position)).getDateTime();
        }
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) return;

        saveOrderIdsAsDateIfNeeded();
        if (updateDraggedItem(fromPosition, toPosition)) {
            saveNewOrder(tableController);
        }
    }

    @Override
    public boolean onCheckCanStartDrag(AbstractDraggableItemViewHolder holder, int position, int x, int y) {
        return getItemViewType(position) == ReceiptsListItem.TYPE_RECEIPT && items.size() > 1;
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return getItemViewType(draggingPosition) == ReceiptsListItem.TYPE_RECEIPT && getItemViewType(dropPosition) == ReceiptsListItem.TYPE_RECEIPT;
    }

    private boolean updateDraggedItem(int oldPosition, int newPosition) {
        Logger.debug(this, "Reordering, from position " + oldPosition + " to position " + newPosition);

        if (getItemViewType(oldPosition) == ReceiptsListItem.TYPE_RECEIPT && getItemViewType(newPosition) == ReceiptsListItem.TYPE_RECEIPT) {

            int oldPositionInItems = items.indexOf(((ReceiptContentItem) listItems.get(oldPosition)).getReceipt());
            int newPositionInItems = items.indexOf(((ReceiptContentItem) listItems.get(newPosition)).getReceipt());

            draggedReceiptNewPosition = newPositionInItems;

            // update custom order id for dragged item
            long newCustomOrderId;
            if (oldPositionInItems < newPositionInItems) { // move down
                newCustomOrderId = items.get(newPositionInItems).getCustomOrderId() - 1;
            } else { // move up
                newCustomOrderId = items.get(newPositionInItems).getCustomOrderId() + 1;
            }

            // updating local list
            final Receipt draggedReceipt = new ReceiptBuilderFactory(items.remove(oldPositionInItems))
                    .setCustomOrderId(newCustomOrderId)
                    .build();
            items.add(newPositionInItems, draggedReceipt);
            updateListItems();

            return true;
        } else {
            return false;
        }
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
            updateListItems();

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

        drawable.mutate(); // hack to prevent fab icon tinting (fab has drawable with the same src)
        DrawableCompat.setTint(drawable, ResourcesCompat.getColor(context.getResources(), R.color.grey_image_tint, null));
        final int pixelPadding = context.getResources().getDimensionPixelOffset(R.dimen.card_image_padding);

        view.setImageDrawable(drawable);
        view.setPadding(pixelPadding, pixelPadding, pixelPadding, pixelPadding);
    }

    @Override
    public void update(List<Receipt> newData) {
        items.clear();
        items.addAll(newData);
        updateListItems();
        notifyDataSetChanged();
    }

    private void updateListItems() {
        listItems.clear();

        // if we don't need headers
        if (!preferences.get(UserPreference.Layout.IncludeReceiptDateInLayout)) {
            for (Receipt receipt : items) {
                listItems.add(new ReceiptContentItem(receipt));
            }
            return;
        }

        // if we need headers
        Receipt previousReceipt = null;

        for (Receipt receipt : items) {
            if (previousReceipt != null) {
                final Date receiptDate = receipt.getDate();
                final Date previousReceiptDate = previousReceipt.getDate();

                final long receiptDays = TimeUnit.MILLISECONDS.toDays(receiptDate.getTime());
                final long previousReceiptDays = TimeUnit.MILLISECONDS.toDays(previousReceiptDate.getTime());

                if (receiptDays != previousReceiptDays) {
                    listItems.add(new ReceiptHeaderItem(receipt.getDate().getTime(),
                            receipt.getFormattedDate(context, preferences.get(UserPreference.General.DateSeparator))));
                }
            } else {
                listItems.add(new ReceiptHeaderItem(receipt.getDate().getTime(),
                        receipt.getFormattedDate(context, preferences.get(UserPreference.General.DateSeparator))));
            }

            listItems.add(new ReceiptContentItem(receipt));
            previousReceipt = receipt;
        }
    }

    @Override
    public int getHeaderPositionForItem(int itemPosition) {
        int headerPosition = 0;
        Preconditions.checkArgument(isHeader(headerPosition), "First item must be header");
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);

        return headerPosition;
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        new ReceiptHeaderReceiptsListViewHolder(header).bindType(listItems.get(headerPosition));
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return getItemViewType(itemPosition) == ReceiptsListItem.TYPE_HEADER;
    }

    private abstract class ReceiptsListViewHolder extends AbstractDraggableItemViewHolder {

        ReceiptsListViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bindType(ReceiptsListItem item);
    }

    private class ReceiptReceiptsListViewHolder extends ReceiptsListViewHolder {

        public TextView price;
        public TextView name;
        public TextView date;
        public TextView category;
        public ImageView syncState;
        public ImageView image;
        ImageView menuButton;

        ReceiptReceiptsListViewHolder(View itemView) {
            super(itemView);

            price = itemView.findViewById(R.id.price);
            name = itemView.findViewById(android.R.id.title);
            category = itemView.findViewById(R.id.card_category);
            syncState = itemView.findViewById(R.id.card_sync_state);
            menuButton = itemView.findViewById(R.id.card_menu);
            image = itemView.findViewById(R.id.card_image);
        }

        @Override
        public void bindType(ReceiptsListItem item) {
            Receipt receipt = ((ReceiptContentItem) item).getReceipt();

            itemView.setOnClickListener(v -> itemClickSubject.onNext(receipt));
            menuButton.setOnClickListener(v -> menuClickSubject.onNext(receipt));
            image.setOnClickListener(v -> imageClickSubject.onNext(receipt));

            if (receipt.hasPDF()) {
                setIcon(image, R.drawable.ic_file_black_24dp);
            } else if (receipt.hasImage()) {
                image.setPadding(0, 0, 0, 0);
                Picasso.with(context)
                        .load(receipt.getImage())
                        .fit()
                        .centerCrop()
                        .into(image);
            } else {
                setIcon(image, R.drawable.ic_receipt_white_24dp);
            }

            price.setText(receipt.getPrice().getCurrencyFormattedPrice());
            name.setText(receipt.getName());

            if (preferences.get(UserPreference.Layout.IncludeReceiptCategoryInLayout)) {
                category.setVisibility(View.VISIBLE);
                category.setText(receipt.getCategory().getName());
            } else {
                category.setVisibility(View.GONE);
            }

            Drawable cloudDisabledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_off_24dp, context.getTheme());
            Drawable notSyncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_queue_24dp, context.getTheme());
            Drawable syncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_done_24dp, context.getTheme());

            if (backupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
                syncState.setClickable(false);
                syncState.setImageDrawable(receipt.getSyncState().isSynced(SyncProvider.GoogleDrive) ? syncedDrawable : notSyncedDrawable);
                syncState.setOnClickListener(null);
            } else {
                syncState.setClickable(true);
                syncState.setImageDrawable(cloudDisabledDrawable);
                syncState.setOnClickListener(view -> navigationHandler.showDialog(new AutomaticBackupsInfoDialogFragment()));
            }

        }

    }

    private class ReceiptHeaderReceiptsListViewHolder extends ReceiptsListViewHolder {
        public TextView date;

        ReceiptHeaderReceiptsListViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.card_date);
        }

        @Override
        public void bindType(ReceiptsListItem item) {
            date.setText(((ReceiptHeaderItem) item).getHeaderText());
        }
    }
}
