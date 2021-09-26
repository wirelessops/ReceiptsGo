package co.smartreceipts.android.receipts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.common.base.Preconditions;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.images.RoundedTransformation;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.receipts.ordering.ReceiptsOrderer;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableCardsAdapter;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.widget.backups.AutomaticBackupsInfoDialogFragment;
import co.smartreceipts.core.sync.provider.SyncProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class ReceiptsAdapter extends DraggableCardsAdapter<Receipt> implements ReceiptsHeaderItemDecoration.StickyHeaderInterface {

    /**
     * List that contains all Receipts from items and needed Headers
     */
    private List<ReceiptsListItem> listItems;

    private final Context context;
    private final UserPreferenceManager preferences;
    private final DateFormatter dateFormatter;
    private final BackupProvidersManager backupProvidersManager;
    private final NavigationHandler navigationHandler;
    private final ReceiptsOrderer receiptsOrderer;
    private final Picasso picasso;
    private final ShowAutomaticBackupsInformationOnClickListener showAutomaticBackupsInformationOnClickListener = new ShowAutomaticBackupsInformationOnClickListener();

    private final PublishSubject<Receipt> itemClickSubject = PublishSubject.create();
    private final PublishSubject<Receipt> imageClickSubject = PublishSubject.create();

    private final Drawable cloudDisabledDrawable;
    private final Drawable notSyncedDrawable;
    private final Drawable syncedDrawable;

    private final Transformation roundedImageTransformation;


    public ReceiptsAdapter(@NonNull Context context,
                           @NonNull UserPreferenceManager preferenceManager,
                           @NonNull DateFormatter dateFormatter,
                           @NonNull BackupProvidersManager backupProvidersManager,
                           @NonNull NavigationHandler navigationHandler,
                           @NonNull ReceiptsOrderer receiptsOrderer,
                           @NonNull Picasso picasso) {
        this.context = Preconditions.checkNotNull(context);
        this.preferences = Preconditions.checkNotNull(preferenceManager);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.backupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.receiptsOrderer = Preconditions.checkNotNull(receiptsOrderer);
        this.picasso = Preconditions.checkNotNull(picasso);

        this.cloudDisabledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_off_24dp, context.getTheme());
        this.notSyncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_queue_24dp, context.getTheme());
        this.syncedDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_cloud_done_24dp, context.getTheme());

        this.listItems = new ArrayList<>();

        roundedImageTransformation = new RoundedTransformation();
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).getListItemType();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @NonNull
    @Override
    public AbstractDraggableItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return viewType == ReceiptsListItem.TYPE_HEADER ? new ReceiptHeaderReceiptsListViewHolder(inflatedView) : new ReceiptReceiptsListViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractDraggableItemViewHolder holder, int position) {
        ((ReceiptsListViewHolder) holder).bindType(listItems.get(position));
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == ReceiptsListItem.TYPE_RECEIPT) {
            return ((ReceiptContentItem) listItems.get(position)).getReceipt().getId();
        } else {
            return ((ReceiptHeaderItem) listItems.get(position)).getDateTime();
        }
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) return;

        int realFromPosition = items.indexOf(((ReceiptContentItem) listItems.get(fromPosition)).getReceipt());
        int realToPosition = items.indexOf(((ReceiptContentItem) listItems.get(toPosition)).getReceipt());

        Logger.debug(this, "Moving receipt from position {} to position {}", realFromPosition, realToPosition);

        List<Receipt> originalItemsList = new ArrayList<>(items);

        // Note: We do this to quick update the UI while the work happens in the background
        final Receipt movedReceipt = items.remove(realFromPosition);
        items.add(realToPosition, movedReceipt);
        updateListItems();

        // TODO: Re-structure to cancel memory-leaks that can happen here
        receiptsOrderer.reorderReceiptsInList(originalItemsList, realFromPosition, realToPosition)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::update, e -> Logger.error(this, "Failed to update receipts list", e));
    }

    @Override
    public boolean onCheckCanStartDrag(AbstractDraggableItemViewHolder holder, int position, int x, int y) {
        return false;
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return getItemViewType(draggingPosition) == ReceiptsListItem.TYPE_RECEIPT && getItemViewType(dropPosition) == ReceiptsListItem.TYPE_RECEIPT;
    }

    @NonNull
    Observable<Receipt> getItemClicks() {
        return itemClickSubject;
    }

    @NonNull
    Observable<Receipt> getImageClicks() {
        return imageClickSubject;
    }

    private void setIcon(ImageView view, @DrawableRes int drawableRes) {
        picasso.cancelRequest(view);

        final Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), drawableRes, context.getTheme());
        if (drawable != null) {
            drawable.mutate(); // hack to prevent fab icon tinting (fab has drawable with the same src)
            DrawableCompat.setTint(drawable, ResourcesCompat.getColor(context.getResources(), R.color.receipt_image_tint, null));
            final int pixelPadding = context.getResources().getDimensionPixelOffset(R.dimen.card_image_padding);
            view.setImageDrawable(drawable);
            view.setPadding(pixelPadding, pixelPadding, pixelPadding, pixelPadding);
        }
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
            final String receiptDate = dateFormatter.getFormattedDate(receipt.getDisplayableDate());
            if (previousReceipt != null) {
                final String previousReceiptDate = dateFormatter.getFormattedDate(previousReceipt.getDisplayableDate());

                if (!receiptDate.equals(previousReceiptDate)) {
                    listItems.add(new ReceiptHeaderItem(receipt.getDate().getTime(), receiptDate));
                }
            } else {
                listItems.add(new ReceiptHeaderItem(receipt.getDate().getTime(), receiptDate));
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

    public int getIndexOfReceipt(Receipt receipt) {
        for (int i = 0; i < listItems.size(); i++) {
            final ReceiptsListItem listItem = listItems.get(i);

            // Note: we can't use just equals for receipts because of index field.
            // The index value depends on the request (if it was made for a trip or for all receipts), so index from search may be different than index here
            if (listItem instanceof ReceiptContentItem && ((ReceiptContentItem) listItem).getReceipt().getId() == receipt.getId()) {
                return i;
            }
        }

        return -1;
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

        ReceiptReceiptsListViewHolder(View itemView) {
            super(itemView);

            price = itemView.findViewById(R.id.text_price);
            name = itemView.findViewById(R.id.text_name);
            category = itemView.findViewById(R.id.text_details);
            syncState = itemView.findViewById(R.id.image_sync_state);
            image = itemView.findViewById(R.id.receipt_image);

            // We use tags here to ensure that we don't need to new an onClickListener for each bind operation
            itemView.setOnClickListener(v -> itemClickSubject.onNext((Receipt) v.getTag()));
            image.setOnClickListener(v -> imageClickSubject.onNext((Receipt) v.getTag()));
        }

        @Override
        public void bindType(ReceiptsListItem item) {
            Receipt receipt = ((ReceiptContentItem) item).getReceipt();

            // Assign the tags to each, so our onclick listeners will respond properly
            itemView.setTag(receipt);
            image.setTag(receipt);

            if (receipt.hasPDF()) {
                setIcon(image, R.drawable.ic_pdf);
            } else if (receipt.hasImage() && receipt.getFile() != null) {
                image.setPadding(0, 0, 0, 0);
                picasso
                        .load(receipt.getFile())
                        .fit()
                        .centerCrop()
                        .transform(roundedImageTransformation)
                        .into(image);
            } else {
                setIcon(image, R.drawable.ic_resource_import);
            }

            price.setText(receipt.getPrice().getCurrencyFormattedPrice());
            name.setText(receipt.getName());

            if (preferences.get(UserPreference.Layout.IncludeReceiptCategoryInLayout)) {
                category.setVisibility(View.VISIBLE);
                category.setText(receipt.getCategory().getName());
            } else {
                category.setVisibility(View.GONE);
            }

            if (backupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
                syncState.setClickable(false);
                if (receipt.getSyncState().isSynced(SyncProvider.GoogleDrive)) {
                    picasso.load(Uri.EMPTY).placeholder(syncedDrawable).into(syncState);
                } else {
                    picasso.load(Uri.EMPTY).placeholder(notSyncedDrawable).into(syncState);
                }
                syncState.setOnClickListener(null);
            } else {
                syncState.setImageDrawable(cloudDisabledDrawable);
                syncState.setOnClickListener(showAutomaticBackupsInformationOnClickListener);
            }
        }

    }

    private class ReceiptHeaderReceiptsListViewHolder extends ReceiptsListViewHolder {
        public TextView date;

        ReceiptHeaderReceiptsListViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.header);
        }

        @Override
        public void bindType(ReceiptsListItem item) {
            date.setText(((ReceiptHeaderItem) item).getHeaderText());
        }
    }

    private final class ShowAutomaticBackupsInformationOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            navigationHandler.showDialog(new AutomaticBackupsInfoDialogFragment());
        }
    }
}
