package co.smartreceipts.android.receipts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alert;
import com.tapadoo.alerter.Alerter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.databinding.ReportReceiptsFragmentBinding;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.fragments.FabClickListener;
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
import co.smartreceipts.android.receipts.creator.ReceiptCreationOption;
import co.smartreceipts.android.receipts.creator.ReceiptCreationOptionsDialog;
import co.smartreceipts.android.receipts.delete.DeleteReceiptDialogFragment;
import co.smartreceipts.android.receipts.editor.ReceiptEditOption;
import co.smartreceipts.android.receipts.editor.ReceiptEditOptionsDialog;
import co.smartreceipts.android.receipts.ordering.ReceiptsOrderer;
import co.smartreceipts.android.search.SearchResultKeeper;
import co.smartreceipts.android.search.Searchable;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.ui.BottomSpacingItemDecoration;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import wb.android.flex.Flex;

public class ReceiptsListFragment extends ReceiptsFragment implements ReceiptsListView, ReceiptTableEventsListener, ReceiptCreateActionView,
        OcrStatusAlerterView, ReceiptAttachmentDialogFragment.Listener, FabClickListener {

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


    private Alerter alerter;
    private Alert alert;

    private ReportReceiptsFragmentBinding binding;

    private Uri imageUri;

    private ActionBarSubtitleUpdatesListener actionBarSubtitleUpdatesListener = new ActionBarSubtitleUpdatesListener();

    private boolean showDateHeaders;
    private ReceiptsHeaderItemDecoration headerItemDecoration;

    private Receipt highlightedReceipt = null;

    @Override
    public void onAttach(@NonNull Context context) {
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

        // listening for create receipt options
        getChildFragmentManager().setFragmentResultListener(ReceiptCreationOptionsDialog.REQUEST_KEY, this,
                (requestKey, result) -> {
                    String creationOption = result.getString(ReceiptCreationOptionsDialog.RESULT_KEY);

                    if (creationOption.equals(ReceiptCreationOption.CAMERA.name())) {
                        createNewReceiptViaCamera();
                    } else if (creationOption.equals(ReceiptCreationOption.TEXT.name())) {
                        createNewReceiptViaPlainText();
                    } else if (creationOption.equals(ReceiptCreationOption.PDF.name())) {
                        createNewReceiptViaFileImport();
                    } else if (creationOption.equals(ReceiptCreationOption.GALLERY.name())) {
                        createNewReceiptViaImageImport();
                    }

                });

        // listening for edit receipt options
        getChildFragmentManager().setFragmentResultListener(ReceiptEditOptionsDialog.REQUEST_KEY, this,
                (requestKey, result) -> {
                    String editOption = result.getString(ReceiptEditOptionsDialog.RESULT_KEY);

                    if (editOption.equals(ReceiptEditOption.EDIT.name())) {
                        analytics.record(Events.Receipts.ReceiptMenuEdit);
                        navigateToEditReceipt(highlightedReceipt);
                    } else if (editOption.equals(ReceiptEditOption.COPY_MOVE.name())) {
                        analytics.record(Events.Receipts.ReceiptMenuMoveCopy);
                        navigationHandler.showDialog(ReceiptMoveCopyDialogFragment.newInstance(highlightedReceipt));
                    } else if (editOption.equals(ReceiptEditOption.DELETE_ATTACHMENT.name())) {
                        analytics.record(Events.Receipts.ReceiptMenuRemoveAttachment);
                        navigationHandler.showDialog(ReceiptRemoveAttachmentDialogFragment.newInstance(highlightedReceipt));
                    } else if (editOption.equals(ReceiptEditOption.DELETE.name())) {
                        analytics.record(Events.Receipts.ReceiptMenuDelete);
                        navigationHandler.showDialog(DeleteReceiptDialogFragment.newInstance(highlightedReceipt));
                    }

                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.debug(this, "onCreateView");

        // Create our OCR drop-down alerter
        this.alerter = Alerter.create(getActivity())
                .setTitle(R.string.ocr_status_title)
                .setBackgroundColorRes(R.color.smart_receipts_colorAccent)
                .setIcon(R.drawable.ic_receipt);

        // And inflate the root view
        binding = ReportReceiptsFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.debug(this, "onViewCreated");

        showDateHeaders = preferenceManager.get(UserPreference.Layout.IncludeReceiptDateInLayout);
        headerItemDecoration = new ReceiptsHeaderItemDecoration(adapter, ReceiptsListItem.TYPE_HEADER);
        if (showDateHeaders) {
            recyclerView.addItemDecoration(headerItemDecoration);
        }

        recyclerView.addItemDecoration(new BottomSpacingItemDecoration());
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

    @Override
    public void onFabClick() {
        ReceiptCreationOptionsDialog dialog = ReceiptCreationOptionsDialog.newInstance();
        dialog.show(getChildFragmentManager(), ReceiptCreationOptionsDialog.TAG);
    }

    @NotNull
    @Override
    public Observable<Receipt> getItemClicks() {
        return adapter.getItemClicks()
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
        if (binding != null) {
            switch (indicator.getState()) {
                case Loading:
                    binding.progress.setVisibility(View.VISIBLE);
                    break;
                case Error:
                case Success:
                    binding.progress.setVisibility(View.GONE);
                    if (indicator.getData().isPresent()) {
                        Toast.makeText(getActivity(), getFlexString(indicator.getData().get()), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    binding.progress.setVisibility(View.GONE);
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
    public void navigateToCropActivity(@NotNull File file, int requestCode) {
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

        binding.progress.setVisibility(View.VISIBLE);

        presenter.handleActivityResult(requestCode, resultCode, data, cachedImageSaveLocation);
    }

    @Override
    public void onDestroyView() {
        Logger.debug(this, "onDestroyView");
        this.alert = null;
        this.alerter = null;
        super.onDestroyView();
        binding = null;
    }

    @Override
    protected PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    @Override
    protected void setNextId(int nextId) {
        binding.textNextId.setText(getString(R.string.next_id, nextId));
    }

    public void showAttachmentDialog(@NonNull final Receipt receipt) {
        ReceiptAttachmentDialogFragment.newInstance(receipt).show(getChildFragmentManager(), ReceiptAttachmentDialogFragment.class.getSimpleName());
    }

    @Override
    public final void showReceiptEditOptionsDialog(final Receipt receipt) {
        ReceiptEditOptionsDialog dialog = ReceiptEditOptionsDialog.newInstance(receipt.getName(), receipt.hasImage() || receipt.hasPDF());
        dialog.show(getChildFragmentManager(), ReceiptEditOptionsDialog.TAG);
    }

    @Override
    protected TableController<Receipt> getTableController() {
        return receiptTableController;
    }

    @Override
    public void onGetSuccess(@NonNull List<Receipt> receipts, @NonNull Trip trip) {
        if (isAdded()) {
            super.onGetSuccess(receipts);

            binding.progress.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (receipts.isEmpty()) {
                binding.noData.setVisibility(View.VISIBLE);
            } else {
                binding.noData.setVisibility(View.GONE);
            }
            updateActionBarTitle(getUserVisibleHint());

            if (getActivity() instanceof SearchResultKeeper) {
                final SearchResultKeeper searchResultKeeper = (SearchResultKeeper) getActivity();

                final Searchable searchResult = searchResultKeeper.getSearchResult();
                if (searchResult instanceof Receipt) {
                    final int index = adapter.getIndexOfReceipt((Receipt) searchResult);
                    if (index >= 0) {
                        recyclerView.smoothScrollToPosition(index);
                    }

                    searchResultKeeper.markSearchResultAsProcessed();
                }
            }

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
    public void createNewReceiptViaCamera() {
        analytics.record(Events.Receipts.AddPictureReceipt);
        imageUri = new CameraInteractionController(this).takePhoto();
    }

    @Override
    public void createNewReceiptViaPlainText() {
        analytics.record(Events.Receipts.AddTextReceipt);
        scrollToStart();
        navigationHandler.navigateToCreateNewReceiptFragment(trip, null, null);
    }

    @Override
    public void createNewReceiptViaFileImport() {
        analytics.record(Events.Receipts.ImportPdfReceipt);

        boolean result = presenter.attachFile(this);
        if (!result) {
            Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void createNewReceiptViaImageImport() {
        analytics.record(Events.Receipts.ImportPictureReceipt);

        boolean result = presenter.attachPicture(this);
        if (!result) {
            Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
        }
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
