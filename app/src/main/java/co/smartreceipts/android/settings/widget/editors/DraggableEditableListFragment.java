package co.smartreceipts.android.settings.widget.editors;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Draggable;
import co.smartreceipts.android.settings.widget.editors.adapters.DraggableEditableCardsAdapter;
import co.smartreceipts.android.utils.log.Logger;

/**
 * Base fragment witch supports Reordering mode and contains toolbar with Add and Reorder/Save options
 */
public abstract class DraggableEditableListFragment<T extends Draggable> extends DraggableListFragment<T, DraggableEditableCardsAdapter<T>>
        implements EditableItemListener<T> {

    private Toolbar toolbar;

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
        toolbar = rootView.findViewById(R.id.toolbar);

        return rootView;
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
    public void onResume() {
        super.onResume();
        getTableController().subscribe(this);
        getTableController().get();
    }

    @Override
    public void onPause() {
        if (!isOnDragMode) {
            saveTableOrdering();
        }
        getTableController().unsubscribe(this);
        recyclerViewDragDropManager.cancelDrag();
        super.onPause();
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
