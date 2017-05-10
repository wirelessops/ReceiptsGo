package co.smartreceipts.android.ocr.widget.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public interface OcrConfigurationView {

    /**
     * Presents the current user's email address (if any)
     */
    void present(@Nullable EmailAddress emailAddress);

    /**
     * Presents the current user's remaining scans
     */
    void present(int remainingScans);

    /**
     * Presents the list of available purchases for this user
     */
    void present(@NonNull List<AvailablePurchase> availablePurchases);

    /**
     * @return an {@link Observable} that will emit a value as to whether the user elects enabled or
     * disable OCR
     */
    @NonNull
    Observable<Boolean> getOcrIsEnabledCheckboxChanged();

    /**
     * @return an {@link Observable} that will emit a value as to whether the user elects to allows
     * us to save images remotely or not
     */
    @NonNull
    Observable<Boolean> getAllowUsToSaveImagesRemotelyCheckboxChanged();

    /**
     * @return an {@link Observable} that emit an available purchase whenever a user chooses to
     * initiate a purchase
     */
    @NonNull
    Observable<AvailablePurchase> getAvailablePurchaseClicks();

    /**
     * @return a {@link Consumer} for interacting with the user's choice for enabling OCR or not
     */
    @NonNull
    Consumer<? super Boolean> getOcrIsEnabledConsumer();

    /**
     * @return a {@link Consumer} for interacting with the user's current select about saving images
     * remotely or not
     */
    @NonNull
    Consumer<? super Boolean> getAllowUsToSaveImagesRemotelyConsumer();
}
