package co.smartreceipts.android.settings.widget.editors.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.settings.widget.editors.EditableItemListener;


public abstract class EditableCardsAdapter<T> extends RecyclerView.Adapter<EditableCardsAdapter.EditableCardsViewHolder>
        implements DraggableItemAdapter<EditableCardsAdapter.EditableCardsViewHolder> {

    protected final List<T> items;

    protected final EditableItemListener<T> listener;

    private boolean isOnDragMode;

    public EditableCardsAdapter(EditableItemListener<T> listener) {
        this(listener, new ArrayList<T>());
    }

    private EditableCardsAdapter(EditableItemListener<T> listener, List<T> items) {
        this.listener = listener;
        this.items = items;

        setHasStableIds(true);
    }

    @Override
    public EditableCardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dragable_editable_card, parent, false);
        return new EditableCardsViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(EditableCardsViewHolder holder, int position) {
            holder.dragHandle.setVisibility(isOnDragMode ? View.VISIBLE : View.GONE);
            holder.delete.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);
            holder.edit.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);
            holder.divider.setVisibility(isOnDragMode ? View.GONE : View.VISIBLE);
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
        notifyItemMoved(fromPosition, toPosition);

        //mProvider.moveItem(fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(EditableCardsViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    @Override
    public boolean onCheckCanStartDrag(EditableCardsViewHolder holder, int position, int x, int y) {
        return isOnDragMode;
    }

    protected static class EditableCardsViewHolder extends AbstractDraggableItemViewHolder{

        public TextView title;
        public TextView summary;
        public View edit;
        public View delete;
        View dragHandle;
        View divider;

        EditableCardsViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(android.R.id.title);
            summary = (TextView) itemView.findViewById(android.R.id.summary);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            divider = itemView.findViewById(R.id.divider);
        }
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

}
