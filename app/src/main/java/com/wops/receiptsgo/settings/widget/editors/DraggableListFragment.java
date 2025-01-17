package com.wops.receiptsgo.settings.widget.editors;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.util.List;

import com.wops.receiptsgo.fragments.WBFragment;
import com.wops.receiptsgo.model.Draggable;
import com.wops.receiptsgo.persistence.database.controllers.TableController;
import com.wops.receiptsgo.persistence.database.controllers.TableEventsListener;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.settings.widget.editors.adapters.DraggableCardsAdapter;

import static android.R.id.list;

/**
 * Basic fragment which contains recycler view with draggable items
 */
public abstract class DraggableListFragment<T extends Draggable, E extends DraggableCardsAdapter<T>> extends WBFragment implements TableEventsListener<T> {

    protected RecyclerView recyclerView;

    protected E adapter;

    protected RecyclerViewDragDropManager recyclerViewDragDropManager = new RecyclerViewDragDropManager();

    protected Integer positionToScroll = null;

    /**
     * @return - the data set used to populate this list fragment
     */
    protected abstract TableController<T> getTableController();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = getView().findViewById(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setDragAndDrop();
    }

    @Override
    public void onDestroyView() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }

        if (recyclerView != null) {
            recyclerView.setItemAnimator(null);
            recyclerView.setAdapter(null);
            recyclerView = null;
        }

        super.onDestroyView();
    }

    protected void setDragAndDrop() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
        }
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();

        // Start dragging after long press
        recyclerViewDragDropManager.setInitiateOnLongPress(true);
        recyclerViewDragDropManager.setInitiateOnMove(false);
        recyclerViewDragDropManager.setDraggingItemRotation(-5);
        recyclerViewDragDropManager.setCheckCanDropEnabled(true);

        recyclerView.setAdapter(recyclerViewDragDropManager.createWrappedAdapter(adapter));
        recyclerView.setItemAnimator(new DraggableItemAnimator());

        recyclerViewDragDropManager.attachRecyclerView(recyclerView);
    }

    protected void scrollToEnd() {
        positionToScroll = recyclerView.getAdapter().getItemCount();
    }

    protected void scrollToStart() {
        positionToScroll = 0;
    }

    @Override
    public void onGetSuccess(@NonNull List<T> list) {
        // Note: The receipts #onGetSuccess(list, trip) calls this super method
        adapter.update(list);
        if (positionToScroll != null) {
            recyclerView.smoothScrollToPosition(positionToScroll);
        }
        positionToScroll = null;
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {

    }

    @Override
    public void onInsertSuccess(@NonNull T t, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        getTableController().get();
    }

    @Override
    public void onInsertFailure(@NonNull T t, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onUpdateSuccess(@NonNull T oldT, @NonNull T newT, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        getTableController().get();
    }

    @Override
    public void onUpdateFailure(@NonNull T oldT, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onDeleteSuccess(@NonNull T t, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        getTableController().get();
    }

    @Override
    public void onDeleteFailure(@NonNull T t, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

}
