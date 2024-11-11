package com.wops.receiptsgo.receipts;

import androidx.appcompat.app.ActionBar;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.PersistenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.settings.widget.editors.DraggableListFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class ReceiptsFragment extends DraggableListFragment<Receipt, ReceiptsAdapter> {

    public static final String TAG = "ReceiptsFragment";

    protected Trip trip;
    private Disposable disposable;

    public static ReceiptsListFragment newListInstance() {
        return new ReceiptsListFragment();
    }

    @Override
    public void onPause() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        super.onPause();
    }

    protected void updateActionBarTitle(boolean updateSubtitle) {
        if (trip == null) {
            return;
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && getUserVisibleHint()) {
            if (updateSubtitle) {
                PersistenceManager persistenceManager = getPersistenceManager();
                if (persistenceManager.getPreferenceManager().get(UserPreference.Receipts.ShowReceiptID)) {
                    disposable = persistenceManager.getDatabase().getNextReceiptAutoIncrementIdHelper()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(receiptId -> {
                                if (isResumed()) {
                                    setNextId(receiptId);
                                }
                            });
                }

                actionBar.setSubtitle(getString(R.string.daily_total, trip.getDailySubTotal().getCurrencyFormattedPrice()));
            }
        }
    }

    protected abstract PersistenceManager getPersistenceManager();

    protected abstract void setNextId(int nextId);

}
