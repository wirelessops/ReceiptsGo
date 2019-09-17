package co.smartreceipts.android.receipts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.github.clans.fab.FloatingActionMenu;
import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding2.view.RxView;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alert;
import com.tapadoo.alerter.Alerter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.fragments.ImportPhotoPdfDialogFragment;
import co.smartreceipts.android.fragments.ReceiptMoveCopyDialogFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.imports.CameraInteractionController;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.widget.alert.OcrStatusAlerterView;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.ReceiptTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.receipts.attacher.ReceiptAttachmentDialogFragment;
import co.smartreceipts.android.receipts.attacher.ReceiptRemoveAttachmentDialogFragment;
import co.smartreceipts.android.receipts.creator.ReceiptCreateActionView;
import co.smartreceipts.android.receipts.delete.DeleteReceiptDialogFragment;
import co.smartreceipts.android.receipts.ordering.ReceiptsOrderer;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.rxbinding2.RxFloatingActionMenu;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import wb.android.flex.Flex;

public class ReceiptsListFragment extends ReceiptsFragment implements ReceiptsListView, ReceiptTableEventsListener, ReceiptCreateActionView,
        OcrStatusAlerterView, ReceiptAttachmentDialogFragment.Listener {

    // Out state
    private static final String OUT_HIGHLIGHTED_RECEIPT = "out_highlighted_receipt";
    private static final String OUT_IMAGE_URI = "out_image_uri";

    @Inject
    ReceiptsListPresenter presenter;

    @Inject
    Flex flex;

    @Inject
    PersistenceManager persistenceManager;

    @Inject
    ConfigurationManager configurationManager;

    @Inject
    Analytics analytics;

    @Inject
    ReceiptTableController receiptTableController;

    @Inject
    BackupProvidersManager backupProvidersManager;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    UserPreferenceManager preferenceManager;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    ReceiptsOrderer receiptsOrderer;

    @Inject
    Picasso picasso;


    @BindView(R.id.progress)
    ProgressBar loadingProgress;

    @BindView(R.id.no_data)
    TextView noDataAlert;

    @BindView(R.id.receipt_action_camera)
    View receiptActionCameraButton;

    @BindView(R.id.receipt_action_text)
    View receiptActionTextButton;

    @BindView(R.id.receipt_action_import)
    View receiptActionImportButton;

    @BindView(R.id.fab_menu)
    FloatingActionMenu floatingActionMenu;

    @BindView(R.id.fab_active_mask)
    View floatingActionMenuActiveMaskView;

    // Non Butter Knife Views
    private Alerter alerter;
    private Alert alert;

    private Unbinder unbinder;

    private Uri imageUri;

    private ActionBarSubtitleUpdatesListener actionBarSubtitleUpdatesListener = new ActionBarSubtitleUpdatesListener();

    private boolean showDateHeaders;
    private ReceiptsHeaderItemDecoration headerItemDecoration;

    private Receipt highlightedReceipt = null;


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.debug(this, "onCreate");
        super.onCreate(savedInstanceState);
        adapter = new ReceiptsAdapter(getContext(), preferenceManager, dateFormatter, backupProvidersManager, navigationHandler, receiptsOrderer, picasso);
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(OUT_IMAGE_URI);
            highlightedReceipt = savedInstanceState.getParcelable(OUT_HIGHLIGHTED_RECEIPT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");

        // Create our OCR drop-down alerter
        this.alerter = Alerter.create(getActivity())
                .setTitle(R.string.ocr_status_title)
                .setBackgroundColorRes(R.color.smart_receipts_colorAccent)
                .setIcon(R.drawable.ic_receipt_white_24dp);

        // And inflate the root view
        return inflater.inflate(R.layout.receipt_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.debug(this, "onViewCreated");

        this.unbinder = ButterKnife.bind(this, view);

        receiptActionTextButton.setVisibility(configurationManager.isEnabled(ConfigurableResourceFeature.TextOnlyReceipts) ? View.VISIBLE : View.GONE);
        floatingActionMenuActiveMaskView.setOnClickListener(v -> {
            // Intentional stub to block click events when this view is active
        });

        showDateHeaders = preferenceManager.get(UserPreference.Layout.IncludeReceiptDateInLayout);
        headerItemDecoration = new ReceiptsHeaderItemDecoration(adapter, ReceiptsListItem.TYPE_HEADER);
        if (showDateHeaders) {
            recyclerView.addItemDecoration(headerItemDecoration);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.debug(this, "onActivityCreated");

        trip = ((ReportInfoFragment) getParentFragment()).getTrip();
        Preconditions.checkNotNull(trip, "A valid trip is required");
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.debug(this, "onStart");
        receiptTableController.subscribe(this);
        receiptTableController.get(trip);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");

        if (showDateHeaders != preferenceManager.get(UserPreference.Layout.IncludeReceiptDateInLayout)) {
            showDateHeaders = preferenceManager.get(UserPreference.Layout.IncludeReceiptDateInLayout);
            if (showDateHeaders) {
                recyclerView.addItemDecoration(headerItemDecoration);
            } else {
                recyclerView.removeItemDecoration(headerItemDecoration);
            }
        }

        presenter.subscribe();
    }

    @NotNull
    @Override
    public Observable<Receipt> getItemClicks() {
        return adapter.getItemClicks();
    }

    @NotNull
    @Override
    public Observable<Receipt> getItemMenuClicks() {
        return adapter.getMenuClicks()
                .doOnNext(receipt -> highlightedReceipt = receipt);
    }

    @NotNull
    @Override
    public Observable<Receipt> getItemImageClicks() {
        return adapter.getImageClicks()
                .doOnNext(receipt -> highlightedReceipt = receipt);
    }

    @NotNull
    @Override
    public Trip getTrip() {
        return trip;
    }

    @NotNull
    @Override
    public StubTableEventsListener<Trip> getActionBarUpdatesListener() {
        return actionBarSubtitleUpdatesListener;
    }


    @Override
    public void present(@NotNull UiIndicator<Integer> indicator) {
        if (loadingProgress != null) {
            switch (indicator.getState()) {
                case Loading:
                    loadingProgress.setVisibility(View.VISIBLE);
                    break;
                case Error:
                case Success:
                    loadingProgress.setVisibility(View.GONE);
                    if (indicator.getData().isPresent()) {
                        Toast.makeText(getActivity(), getFlexString(indicator.getData().get()), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    loadingProgress.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void navigateToEditReceipt(@NotNull Receipt receipt) {
        navigationHandler.navigateToEditReceiptFragment(trip, receipt);
    }

    @Override
    public void navigateToReceiptImage(@NotNull Receipt receipt) {
        navigationHandler.navigateToViewReceiptImage(receipt);
    }

    @Override
    public void navigateToCropActivity(File file, int requestCode) {
        navigationHandler.navigateToCropActivity(this, file, requestCode);
    }

    @Override
    public void navigateToReceiptPdf(@NotNull Receipt receipt) {
        navigationHandler.navigateToViewReceiptPdf(receipt);
    }

    @Override
    public void navigateToCreateReceipt(@Nullable File file, @Nullable OcrResponse ocrResponse) {
        navigationHandler.navigateToCreateNewReceiptFragment(trip, file, ocrResponse);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        updateActionBarTitle(true);
    }

    @Override
    public void onPause() {
        presenter.unsubscribe();

        floatingActionMenu.close(false);
        super.onPause();
    }

    @Override
    public void onStop() {
        receiptTableController.unsubscribe(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(OUT_IMAGE_URI, imageUri);
        outState.putParcelable(OUT_HIGHLIGHTED_RECEIPT, highlightedReceipt);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Logger.debug(this, "onActivityResult");
        Logger.debug(this, "Result Code: {}", resultCode);
        Logger.debug(this, "Request Code: {}", requestCode);

        // Need to make this call here, since users with "Don't keep activities" will hit this call
        // before any of onCreate/onStart/onResume is called. This should restore our current trip (what
        // onResume() would normally do to prevent a variety of crashes that we might encounter
        if (trip == null) {
            trip = ((ReportInfoFragment) getParentFragment()).getTrip();
        }

        // Null out the last request
        final Uri cachedImageSaveLocation = imageUri;
        imageUri = null;

        loadingProgress.setVisibility(View.VISIBLE);

        presenter.handleActivityResult(requestCode, resultCode, data, cachedImageSaveLocation);
    }

    @Override
    public void onDestroyView() {
        Logger.debug(this, "onDestroyView");
        this.alert = null;
        this.alerter = null;
        this.unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public void showAttachmentDialog(@NonNull final Receipt receipt) {
        ReceiptAttachmentDialogFragment.newInstance(receipt).show(getChildFragmentManager(), ReceiptAttachmentDialogFragment.class.getSimpleName());
    }

    @Override
    public final void showReceiptMenu(final Receipt receipt) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(receipt.getName())
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.cancel());

        final String receiptActionDelete = getString(R.string.receipt_dialog_action_delete);
        final String receiptActionMoveCopy = getString(R.string.receipt_dialog_action_move_copy);
        final String receiptActionRemoveAttachment = getString(R.string.receipt_dialog_action_remove_attachment);
        final String[] receiptActions;
        if (receipt.getFile() != null) {
            receiptActions = new String[]{receiptActionDelete, receiptActionMoveCopy, receiptActionRemoveAttachment};
        } else {
            receiptActions = new String[]{receiptActionDelete, receiptActionMoveCopy};
        }
        builder.setItems(receiptActions, (dialog, item) -> {
            final String selection = receiptActions[item];
            if (selection != null) {
                if (selection.equals(receiptActionDelete)) { // Delete Receipt
                    analytics.record(Events.Receipts.ReceiptMenuDelete);
                    final DeleteReceiptDialogFragment deleteReceiptDialogFragment = DeleteReceiptDialogFragment.newInstance(receipt);
                    navigationHandler.showDialog(deleteReceiptDialogFragment);

                } else if (selection.equals(receiptActionMoveCopy)) {// Move-Copy
                    analytics.record(Events.Receipts.ReceiptMenuMoveCopy);
                    ReceiptMoveCopyDialogFragment.newInstance(receipt).show(getFragmentManager(), ReceiptMoveCopyDialogFragment.TAG);

                } else if (selection.equals(receiptActionRemoveAttachment)) { // Remove Attachment
                    analytics.record(Events.Receipts.ReceiptMenuRemoveAttachment);
                    navigationHandler.showDialog(ReceiptRemoveAttachmentDialogFragment.newInstance(receipt));
                }
            }
            dialog.cancel();
        });
        builder.show();
    }

    @Override
    protected TableController<Receipt> getTableController() {
        return receiptTableController;
    }

    @Override
    public void onGetSuccess(@NonNull List<Receipt> receipts, @NonNull Trip trip) {
        if (isAdded()) {
            super.onGetSuccess(receipts);

            loadingProgress.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (receipts.isEmpty()) {
                noDataAlert.setVisibility(View.VISIBLE);
            } else {
                noDataAlert.setVisibility(View.INVISIBLE);
            }
            updateActionBarTitle(getUserVisibleHint());

        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e, @NonNull Trip trip) {
        Toast.makeText(getActivity(), R.string.database_get_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetSuccess(@NonNull List<Receipt> list) {
        // TODO: Respond?
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {
        Toast.makeText(getActivity(), R.string.database_get_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInsertSuccess(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            receiptTableController.get(trip);
        }
    }

    @Override
    public void onInsertFailure(@NonNull Receipt receipt, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded() && databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
                if (newReceipt.getFile() != null && newReceipt.getFileLastModifiedTime() != oldReceipt.getFileLastModifiedTime()) {
                    final int stringId;
                    if (oldReceipt.getFile() != null) {
                        if (newReceipt.hasImage()) {
                            stringId = R.string.toast_receipt_image_replaced;
                        } else {
                            stringId = R.string.toast_receipt_pdf_replaced;
                        }
                    } else {
                        if (newReceipt.hasImage()) {
                            stringId = R.string.toast_receipt_image_added;
                        } else {
                            stringId = R.string.toast_receipt_pdf_added;
                        }
                    }
                    Toast.makeText(getActivity(), getString(stringId, newReceipt.getName()), Toast.LENGTH_SHORT).show();


                    if (getActivity() != null && getActivity().getIntent() != null) {
                        presenter.markIntentAsProcessed(getActivity().getIntent());
                    }
                }
            }
            // But still refresh for sync operations
            receiptTableController.get(trip);
        }
    }

    @Override
    public void onUpdateFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded() && databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            receiptTableController.get(trip);
        }
    }

    @Override
    public void onDeleteFailure(@NonNull Receipt receipt, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded() && databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCopySuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        if (isAdded()) {
            receiptTableController.get(trip);
            Toast.makeText(getActivity(), getFlexString(R.string.toast_receipt_copy), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCopyFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.COPY_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMoveSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        if (isAdded()) {
            receiptTableController.get(trip);
            Toast.makeText(getActivity(), getFlexString(R.string.toast_receipt_move), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMoveFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.MOVE_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void displayReceiptCreationMenuOptions() {
        if (floatingActionMenuActiveMaskView.getVisibility() != View.VISIBLE) { // avoid duplicate animations
            floatingActionMenuActiveMaskView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.out_from_bottom_right));
            floatingActionMenuActiveMaskView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideReceiptCreationMenuOptions() {
        if (floatingActionMenuActiveMaskView.getVisibility() != View.GONE) { // avoid duplicate animations
            floatingActionMenuActiveMaskView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.in_to_bottom_right));
            floatingActionMenuActiveMaskView.setVisibility(View.GONE);
        }
    }

    @Override
    public void createNewReceiptViaCamera() {
        imageUri = new CameraInteractionController(this).takePhoto();
    }

    @Override
    public void createNewReceiptViaPlainText() {
        scrollToStart();
        navigationHandler.navigateToCreateNewReceiptFragment(trip, null, null);
    }

    @Override
    public void createNewReceiptViaFileImport() {
        final ImportPhotoPdfDialogFragment fragment = new ImportPhotoPdfDialogFragment();
        fragment.show(getChildFragmentManager(), ImportPhotoPdfDialogFragment.TAG);
    }

    @NonNull
    @Override
    public Observable<Boolean> getCreateNewReceiptMenuButtonToggles() {
        return RxFloatingActionMenu.toggleChanges(floatingActionMenu);
    }

    @NonNull
    @Override
    public Observable<Object> getCreateNewReceiptFromCameraButtonClicks() {
        return RxView.clicks(receiptActionCameraButton);
    }

    @NonNull
    @Override
    public Observable<Object> getCreateNewReceiptFromImportedFileButtonClicks() {
        return RxView.clicks(receiptActionImportButton);
    }

    @NonNull
    @Override
    public Observable<Object> getCreateNewReceiptFromPlainTextButtonClicks() {
        return RxView.clicks(receiptActionTextButton);
    }

    @Override
    public void displayOcrStatus(@NonNull UiIndicator<String> ocrStatusIndicator) {
        if (ocrStatusIndicator.getState() == UiIndicator.State.Loading) {

            present(UiIndicator.loading());

            if (alert == null) {
                alerter.setText(ocrStatusIndicator.getData().get());
                alert = alerter.show();
                alert.setEnableInfiniteDuration(true);
            } else {
                alert.setText(ocrStatusIndicator.getData().get());
            }
        } else if (alert != null) {
            Alerter.hide();
            alert = null;

            present(UiIndicator.idle());
        }
    }

    @Override
    public void setImageUri(@NonNull Uri uri) {
        imageUri = uri;
    }

    @Override
    public Receipt getHighlightedReceipt() {
        return highlightedReceipt;
    }

    @Override
    public void resetHighlightedReceipt() {
        highlightedReceipt = null;
    }

    private class ActionBarSubtitleUpdatesListener extends StubTableEventsListener<Trip> {

        @Override
        public void onGetSuccess(@NonNull List<Trip> list) {
            if (isAdded()) {
                updateActionBarTitle(getUserVisibleHint());
            }
        }
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }

}
