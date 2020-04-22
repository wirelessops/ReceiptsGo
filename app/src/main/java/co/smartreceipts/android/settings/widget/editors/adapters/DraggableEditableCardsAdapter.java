package co.smartreceipts.android.settings.widget.editors.adapters;

import androidx.annotation.NonNull;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Draggable;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;

public abstract class DraggableEditableCardsAdapter<T extends Draggable> extends DraggableCardsAdapter<T> {

    protected final EditableItemListener<T> listener;
    protected boolean isOnDragMode;

    public DraggableEditableCardsAdapter(EditableItemListener<T> listener) {
        this(listener, new ArrayList<>());
    }

    private DraggableEditableCardsAdapter(EditableItemListener<T> listener, List<T> items) {
        super(items);
        this.listener = listener;
    }

    @Override
    public boolean onCheckCanStartDrag(@NonNull AbstractDraggableItemViewHolder holder, int position, int x, int y) {
        return isOnDragMode;
    }

    public void switchMode(boolean isDraggable) {
        isOnDragMode = isDraggable;
        notifyDataSetChanged();
    }

    public abstract void saveNewOrder(TableController<T> tableController);
}
