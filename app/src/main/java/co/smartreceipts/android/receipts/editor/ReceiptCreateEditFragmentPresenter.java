package co.smartreceipts.android.receipts.editor;

import androidx.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.autocomplete.receipt.ReceiptAutoCompleteField;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.analytics.log.Logger;
import dagger.internal.Preconditions;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class ReceiptCreateEditFragmentPresenter {

    private final ReceiptCreateEditFragment fragment;
    private final UserPreferenceManager preferenceManager;
    private final PurchaseManager purchaseManager;
    private final PurchaseWallet purchaseWallet;
    private final ReceiptTableController receiptTableController;
    private final CompositeDisposable compositeDisposable;
    private int positionToRemoveOrAdd;

    ReceiptCreateEditFragmentPresenter(@NonNull ReceiptCreateEditFragment fragment,
                                       @NonNull UserPreferenceManager preferenceManager,
                                       @NonNull PurchaseManager purchaseManager,
                                       @NonNull PurchaseWallet purchaseWallet,
                                       @NonNull ReceiptTableController receiptTableController) {
        this.fragment = Preconditions.checkNotNull(fragment);
        this.preferenceManager = Preconditions.checkNotNull(preferenceManager);
        this.purchaseManager = Preconditions.checkNotNull(purchaseManager);
        this.purchaseWallet = Preconditions.checkNotNull(purchaseWallet);
        this.receiptTableController = Preconditions.checkNotNull(receiptTableController);
        this.compositeDisposable = new CompositeDisposable();
    }

    public void subscribe() {
        compositeDisposable.add(fragment.getHideAutoCompleteVisibilityClick()
                .flatMap(autoCompleteClickEvent -> {
                    positionToRemoveOrAdd = autoCompleteClickEvent.getPosition();
                    if (autoCompleteClickEvent.getType() == ReceiptAutoCompleteField.Name) {
                        return updateReceipt(autoCompleteClickEvent.getItem().getFirstItem(),
                                new ReceiptBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setNameHiddenFromAutoComplete(true)
                                        .build());
                    } else if (autoCompleteClickEvent.getType() == ReceiptAutoCompleteField.Comment) {
                        return updateReceipt(autoCompleteClickEvent.getItem().getFirstItem(),
                                new ReceiptBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setCommentHiddenFromAutoComplete(true)
                                        .build());
                    } else {
                        throw new UnsupportedOperationException("Unknown type: " + autoCompleteClickEvent.getType());
                    }
                })
                .subscribe(receiptOptional -> {
                    if (receiptOptional.isPresent()) {
                        fragment.removeValueFromAutoComplete(positionToRemoveOrAdd);
                    }
                }));

        compositeDisposable.add(fragment.getUnHideAutoCompleteVisibilityClick()
                .flatMap(autoCompleteClickEvent -> {
                    if (autoCompleteClickEvent.getType() == ReceiptAutoCompleteField.Name) {
                        return updateReceipt(autoCompleteClickEvent.getItem().getFirstItem(),
                                new ReceiptBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setNameHiddenFromAutoComplete(false)
                                        .build());
                    } else if (autoCompleteClickEvent.getType() == ReceiptAutoCompleteField.Comment) {
                        return updateReceipt(autoCompleteClickEvent.getItem().getFirstItem(),
                                new ReceiptBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setCommentHiddenFromAutoComplete(false)
                                        .build());
                    } else {
                        throw new UnsupportedOperationException("Unknown type: " + autoCompleteClickEvent.getType());
                    }
                })
                .subscribe(receiptOptional -> {
                    if (receiptOptional.isPresent()) {
                        fragment.sendAutoCompleteUnHideEvent(positionToRemoveOrAdd);
                    } else {
                        fragment.displayAutoCompleteError();
                    }
                }));
    }

    public void unsubscribe() {
        compositeDisposable.clear();
    }

    boolean isIncludeTaxField() {
        return preferenceManager.get(UserPreference.Receipts.IncludeTaxField);
    }

    boolean isUsePreTaxPrice() {
        return preferenceManager.get(UserPreference.Receipts.UsePreTaxPrice);
    }

    float getDefaultTaxPercentage() {
        return preferenceManager.get(UserPreference.Receipts.DefaultTaxPercentage);
    }

    boolean isReceiptDateDefaultsToReportStartDate() {
        return preferenceManager.get(UserPreference.Receipts.ReceiptDateDefaultsToReportStartDate);
    }

    boolean isReceiptsDefaultAsReimbursable() {
        return preferenceManager.get(UserPreference.Receipts.ReceiptsDefaultAsReimbursable);
    }

    boolean isMatchReceiptCommentToCategory() {
        return preferenceManager.get(UserPreference.Receipts.MatchReceiptCommentToCategory);
    }

    boolean isMatchReceiptNameToCategory() {
        return preferenceManager.get(UserPreference.Receipts.MatchReceiptNameToCategory);
    }

    String getDefaultCurrency() {
        return preferenceManager.get(UserPreference.General.DefaultCurrency);
    }

    boolean isDefaultToFullPage() {
        return preferenceManager.get(UserPreference.Receipts.DefaultToFullPage);
    }

    String getDateSeparator() {
        return preferenceManager.get(UserPreference.General.DateSeparator);
    }

    boolean isPredictCategories() {
        return preferenceManager.get(UserPreference.Receipts.PredictCategories);
    }

    boolean isUsePaymentMethods() {
        return preferenceManager.get(UserPreference.Receipts.UsePaymentMethods);
    }

    boolean isShowReceiptId() {
        return preferenceManager.get(UserPreference.Receipts.ShowReceiptID);
    }

    boolean isEnableAutoCompleteSuggestions() {
        return preferenceManager.get(UserPreference.Receipts.EnableAutoCompleteSuggestions);
    }

    void initiatePurchase() {
        purchaseManager.initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.ExchangeRate);
    }

    boolean hasActivePlusPurchase() {
        return purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
    }

    boolean checkReceipt(Date date) {
        if (date == null) {
            fragment.showDateError();
            return false;
        }

        if (!fragment.getParentTrip().isDateInsideTripBounds(date)) {
            fragment.showDateWarning();
        }

        return true;
    }

    void saveReceipt(Date date, TimeZone timeZone, String price, String tax,
                     String exchangeRate, String comment, PaymentMethod paymentMethod,
                     boolean isReimbursable, boolean isFullPage,
                     String name, Category category, String currency,
                     String extraText1, String extraText2, String extraText3) {

        final Receipt receipt = fragment.getEditableItem();
        final Trip parentTrip = fragment.getParentTrip();

        final ReceiptBuilderFactory builderFactory = (receipt == null) ? new ReceiptBuilderFactory(-1) : new ReceiptBuilderFactory(receipt);
        builderFactory.setName(name)
                .setTrip(parentTrip)
                .setDate((Date) date.clone())
                .setTimeZone(timeZone)
                .setPrice(price)
                .setTax(tax)
                .setExchangeRate(new ExchangeRateBuilderFactory().setBaseCurrency(currency)
                        .setRate(parentTrip.getTripCurrency(), exchangeRate)
                        .build())
                .setCategory(category)
                .setCurrency(currency)
                .setComment(comment)
                .setPaymentMethod(paymentMethod)
                .setIsReimbursable(isReimbursable)
                .setIsFullPage(isFullPage)
                .setExtraEditText1(extraText1)
                .setExtraEditText2(extraText2)
                .setExtraEditText3(extraText3);
                // Note: We don't set the custom_order_id. This happens in the ReceiptTableActionAlterations

        if (receipt == null) {
            receiptTableController.insert(builderFactory.setFile(fragment.getFile()).build(), new DatabaseOperationMetadata());
        } else {
            receiptTableController.update(receipt, builderFactory.build(), new DatabaseOperationMetadata());
        }
    }

    boolean deleteReceiptFileIfUnused() {
        if (fragment.getEditableItem() == null && fragment.getFile() != null) {
            if (fragment.getFile().delete()) {
                Logger.info(this, "Deleting receipt file as we're not saving it");
                return true;
            }
        }
        return false;
    }

    public Observable<Optional<Receipt>> updateReceipt(Receipt oldReceipt, Receipt newReceipt) {
        return receiptTableController.update(oldReceipt, newReceipt, new DatabaseOperationMetadata());
    }

}
