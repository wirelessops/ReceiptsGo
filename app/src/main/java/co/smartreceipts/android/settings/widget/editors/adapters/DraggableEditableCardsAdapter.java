package co.smartreceipts.android.settings.widget.editors.adapters;

import android.support.v7.widget.RecyclerView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Draggable;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;


public abstract class DraggableEditableCardsAdapter<T extends Draggable> extends RecyclerView.Adapter<AbstractDraggableItemViewHolder>
        implements DraggableItemAdapter<AbstractDraggableItemViewHolder> {

    protected final List<T> items;
    protected final EditableItemListener<T> listener;
    protected boolean isOnDragMode;

    public DraggableEditableCardsAdapter(EditableItemListener<T> listener) {
        this(listener, new ArrayList<T>());
    }

    private DraggableEditableCardsAdapter(EditableItemListener<T> listener, List<T> items) {
        this.listener = listener;
        this.items = items;

        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public abstract long getItemId(int position);

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        T movedItem = items.remove(fromPosition);
        items.add(toPosition, movedItem);
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(AbstractDraggableItemViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    @Override
    public boolean onCheckCanStartDrag(AbstractDraggableItemViewHolder holder, int position, int x, int y) {
        return isOnDragMode;
    }

    public void update(List<T> newData)  {
        items.clear();
        items.addAll(newData);
        notifyDataSetChanged();
    }

    public void switchMode(boolean isDragable) {
        isOnDragMode = isDragable;
        notifyDataSetChanged();
    }

    public abstract void saveNewOrder(TableController<T> tableController);

}
