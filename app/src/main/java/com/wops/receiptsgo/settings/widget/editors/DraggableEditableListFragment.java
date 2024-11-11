package com.wops.receiptsgo.settings.widget.editors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.databinding.SimpleRecyclerViewBinding;
import com.wops.receiptsgo.model.Draggable;
import com.wops.receiptsgo.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import co.smartreceipts.analytics.log.Logger;

/**
 * Base fragment witch supports Reordering mode and contains toolbar with Add and Reorder/Save options
 */
public abstract class DraggableEditableListFragment<T extends Draggable> extends DraggableListFragment<T, DraggableEditableCardsAdapter<T>>
        implements EditableItemListener<T> {

    private Toolbar toolbar;

    private boolean isOnDragMode = false;
    private SimpleRecyclerViewBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = getAdapter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SimpleRecyclerViewBinding.inflate(inflater, container, false);
        toolbar = binding.toolbar.toolbar;

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
                Toast.makeText(getContext(), R.string.toast_reorder_hint, Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_settings_save_order:
                saveTableOrdering();
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

    @CallSuper
    protected void saveTableOrdering() {
        Logger.debug(this, "saveTableOrdering");
        adapter.saveNewOrder(getTableController());
    }

    @Override
    public void onStart() {
        super.onStart();
        getTableController().subscribe(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getTableController().get();
    }

    @Override
    public void onPause() {
        if (!isOnDragMode) {
            saveTableOrdering();
        }
        recyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onStop() {
        getTableController().unsubscribe(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * @return the {@link DraggableEditableCardsAdapter} that is being used by this fragment
     */
    protected abstract DraggableEditableCardsAdapter<T> getAdapter();

    /**
     * Shows the proper message in order to assist the user with inserting an item
     */
    protected abstract void addItem();
}
