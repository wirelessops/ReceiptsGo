package co.smartreceipts.android.receipts.editor.date;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import java.sql.Date;

import io.reactivex.Observable;

/**
 * A view contract for managing the receipt date
 */
public interface ReceiptDateView {

    /**
     * @return an {@link Observable} that will emit a {@link Date} any time the user changes the value for
     * the receipt date
     */
    @NonNull
    @UiThread
    Observable<Date> getReceiptDateChanges();
}
