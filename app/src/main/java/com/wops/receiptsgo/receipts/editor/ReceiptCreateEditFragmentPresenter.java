package com.wops.receiptsgo.receipts.editor;

import androidx.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import java.sql.Date;
import java.util.TimeZone;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.autocomplete.receipt.ReceiptAutoCompleteField;
import com.wops.receiptsgo.model.Category;
import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.factory.ExchangeRateBuilderFactory;
import com.wops.receiptsgo.model.factory.ReceiptBuilderFactory;
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import dagger.internal.Preconditions;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class ReceiptCreateEditFragmentPresenter {

    private final ReceiptCreateEditFragment fragment;
    private final UserPreferenceManager preferenceManager;
    private final ReceiptTableController receiptTableController;
    private final CompositeDisposable compositeDisposable;
    private int positionToRemoveOrAdd;

    ReceiptCreateEditFragmentPresenter(@NonNull ReceiptCreateEditFragment fragment,
                                       @NonNull UserPreferenceManager preferenceManager,
                                       @NonNull ReceiptTableController receiptTableController) {
        this.fragment = Preconditions.checkNotNull(fragment);
        this.preferenceManager = Preconditions.checkNotNull(preferenceManager);
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

    boolean isIncludeTax2Field() {
        return preferenceManager.get(UserPreference.Receipts.IncludeTax2Field);
    }

    boolean isUsePreTaxPrice() {
        return preferenceManager.get(UserPreference.Receipts.UsePreTaxPrice);
    }

    float getDefaultTaxPercentage() {
        return preferenceManager.get(UserPreference.Receipts.DefaultTaxPercentage);
    }

    float getDefaultTax2Percentage() {
        return preferenceManager.get(UserPreference.Receipts.DefaultTax2Percentage);
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

    void saveReceipt(Date date, TimeZone timeZone, String price, String tax, String tax2,
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
                .setTax2(tax2)
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
