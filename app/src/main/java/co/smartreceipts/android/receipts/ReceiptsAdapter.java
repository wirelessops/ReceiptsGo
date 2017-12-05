package co.smartreceipts.android.receipts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableCardsAdapter;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.widget.backups.AutomaticBackupsInfoDialogFragment;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ReceiptsAdapter extends DraggableCardsAdapter<Receipt> {

    private final UserPreferenceManager preferences;
    private final BackupProvidersManager backupProvidersManager;
    private final NavigationHandler navigationHandler;
    private final Context context;
    private final TableController<Receipt> tableController;

    private final PublishSubject<Receipt> onClickSubject = PublishSubject.create();

    public ReceiptsAdapter(Context context, TableController<Receipt> tableController, UserPreferenceManager preferenceManager,
                           BackupProvidersManager backupProvidersManager, NavigationHandler navigationHandler) {
        super();
        this.preferences = Preconditions.checkNotNull(preferenceManager);
        this.context = Preconditions.checkNotNull(context);
        this.backupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.tableController = Preconditions.checkNotNull(tableController);
    }

    @Override
    public AbstractDraggableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receipt_card, parent, false);
        return new ReceiptViewHolder(inflatedView);    }

    @Override
    public void onBindViewHolder(AbstractDraggableItemViewHolder holder, int position) {
        ReceiptViewHolder receiptHolder = (ReceiptViewHolder) holder;
        Receipt receipt = items.get(position);

        receiptHolder.itemView.setOnClickListener(v -> onClickSubject.onNext(receipt));

        receiptHolder.order.setText(String.valueOf(receipt.getCustomOrderId()));

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
        }else {
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
        super.onMoveItem(fromPosition, toPosition);
        saveNewOrder(tableController);
    }

    @Override
    public boolean onCheckCanStartDrag(AbstractDraggableItemViewHolder holder, int position, int x, int y) {
        return true;
    }

    @Override
    public void saveNewOrder(TableController<Receipt> tableController) {
        for (Receipt item : items) {
            if (item.getCustomOrderId() != items.indexOf(item)) {
                tableController.update(item, new ReceiptBuilderFactory(item)
                                .setCustomOrderId(items.indexOf(item))
                                .build(),
                        new DatabaseOperationMetadata());
            }
        }


    }

    public Observable<Receipt> getItemClickStream() {
        return onClickSubject;
    }

    private static class ReceiptViewHolder extends AbstractDraggableItemViewHolder {

        public TextView price;
        public TextView name;
        public TextView date;
        public TextView category;
        public TextView marker;
        public ImageView syncState;

        private TextView order;

        ReceiptViewHolder(View itemView) {
            super(itemView);

            price = itemView.findViewById(R.id.price);
            name = itemView.findViewById(android.R.id.title);
            date = itemView.findViewById(android.R.id.summary);
            category = itemView.findViewById(android.R.id.text1);
            marker = itemView.findViewById(android.R.id.text2);
            syncState = itemView.findViewById(R.id.card_sync_state);

            order = itemView.findViewById(R.id.tmp_order);
        }
    }
}
