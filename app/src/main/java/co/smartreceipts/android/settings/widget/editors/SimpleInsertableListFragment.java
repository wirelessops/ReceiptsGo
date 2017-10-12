package co.smartreceipts.android.settings.widget.editors;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;

import static android.R.id.list;

public abstract class SimpleInsertableListFragment<T> extends WBFragment implements EditableItemListener<T>, TableEventsListener<T> {

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private DraggableEditableCardsAdapter<T> adapter;

    private RecyclerViewDragDropManager recyclerViewDragDropManager = new RecyclerViewDragDropManager();

    private boolean isOnDragMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = getAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.simple_recycler_view, container, false);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) getView().findViewById(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setDragAndDrop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO: 08.09.2017 translate reorder string
        inflater.inflate(R.menu.menu_settings_add_drag, menu);

        // show just Save button if it's drag&drop mode
        menu.findItem(R.id.menu_settings_drag).setVisible(!isOnDragMode);
        menu.findItem(R.id.menu_settings_add).setVisible(!isOnDragMode);
        menu.findItem(R.id.menu_settings_save_order).setVisible(isOnDragMode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_add:
                addItem();
                break;
            case R.id.menu_settings_drag:
                isOnDragMode = true;
                getActivity().invalidateOptionsMenu();
                adapter.switchMode(isOnDragMode);
                break;
            case R.id.menu_settings_save_order:
                // TODO: 08.09.2017 save, show progress bar
                isOnDragMode = false;
                getActivity().invalidateOptionsMenu();
                adapter.switchMode(isOnDragMode);
                break;
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getTableController().subscribe(this);
        getTableController().get();
        // TODO: 06.10.2017 show snackBar or at least toast? with a tip about long press if isOnDragMode = true
        Toast.makeText(getContext(), "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        getTableController().unsubscribe(this);
        recyclerViewDragDropManager.cancelDrag();
        super.onPause();
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

    /**
     * @return the {@link DraggableEditableCardsAdapter} that is being used by this fragment
     */
    protected abstract DraggableEditableCardsAdapter<T> getAdapter();

    /**
     * @return - the data set used to populate this list fragment
     */
    protected abstract TableController<T> getTableController();

    /**
     * Shows the proper message in order to assist the user with inserting an item
     */
    protected abstract void addItem();

    private void setDragAndDrop() {
        recyclerViewDragDropManager.release();
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();

        // Start dragging after long press
        recyclerViewDragDropManager.setInitiateOnLongPress(true);
        recyclerViewDragDropManager.setInitiateOnMove(false);
        recyclerViewDragDropManager.setDraggingItemRotation(-5);

        recyclerView.setAdapter(recyclerViewDragDropManager.createWrappedAdapter(adapter));
        recyclerView.setItemAnimator(new DraggableItemAnimator());

        recyclerViewDragDropManager.attachRecyclerView(recyclerView);
    }

    @Override
    public void onGetSuccess(@NonNull List<T> list) {
        adapter.update(list);
        setDragAndDrop();
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
