package com.wops.receiptsgo.receipts;

import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// source: https://stackoverflow.com/a/44327350
public class ReceiptsHeaderItemDecoration extends RecyclerView.ItemDecoration {

    private StickyHeaderInterface listener;
    private int headerLayoutResId;

    public ReceiptsHeaderItemDecoration(@NonNull StickyHeaderInterface listener, int headerLayoutResId) {
        this.listener = listener;
        this.headerLayoutResId = headerLayoutResId;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        View topChild = parent.getChildAt(0);
        if (topChild == null) {
            return;
        }

        int topChildPosition = parent.getChildAdapterPosition(topChild);
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return;
        }

        View currentHeader = getHeaderViewForItem(topChildPosition, parent);
        fixLayoutSize(parent, currentHeader);
        int contactPoint = currentHeader.getBottom();
        View childInContact = getChildInContact(parent, contactPoint);
        if (childInContact == null) {
            return;
        }

        final int childPosition = parent.getChildAdapterPosition(childInContact);
        if (childPosition < 0) {
            return;
        }

        if (listener.isHeader(childPosition)) {
            moveHeader(c, currentHeader, childInContact);
            return;
        }

        drawHeader(c, currentHeader);
    }

    private View getHeaderViewForItem(int itemPosition, RecyclerView parent) {
        int headerPosition = listener.getHeaderPositionForItem(itemPosition);
        View header = LayoutInflater.from(parent.getContext()).inflate(headerLayoutResId, parent, false);
        listener.bindHeaderData(header, headerPosition);
        return header;
    }

    private void drawHeader(Canvas c, View header) {
        c.save();
        c.translate(0, 0);
        header.draw(c);
        c.restore();
    }

    private void moveHeader(Canvas c, View currentHeader, View nextHeader) {
        c.save();
        c.translate(0, nextHeader.getTop() - currentHeader.getHeight());
        currentHeader.draw(c);
        c.restore();
    }

    private View getChildInContact(RecyclerView parent, int contactPoint) {
        View childInContact = null;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getBottom() > contactPoint) {
                if (child.getTop() <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child;
                    break;
                }
            }
        }
        return childInContact;
    }

    /**
     * Properly measures and layouts the top sticky header.
     *
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private void fixLayoutSize(ViewGroup parent, View view) {

        // Specs for parent (RecyclerView)
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        // Specs for children (headers)
        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams().width);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams().height);

        view.measure(childWidthSpec, childHeightSpec);

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    public interface StickyHeaderInterface {

        /**
         * This method gets called by {@link ReceiptsHeaderItemDecoration} to fetch the position of the header item in the adapter
         * that is used for (represents) item at specified position.
         *
         * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the header item.
         * @return int. Position of the header item in the adapter.
         */
        int getHeaderPositionForItem(int itemPosition);

        /**
         * This method gets called by {@link ReceiptsHeaderItemDecoration} to setup the header View.
         *
         * @param header         View. Header to set the data on.
         * @param headerPosition int. Position of the header item in the adapter.
         */
        void bindHeaderData(View header, int headerPosition);

        /**
         * This method gets called by {@link ReceiptsHeaderItemDecoration} to verify whether the item represents a header.
         *
         * @param itemPosition int.
         * @return true, if item at the specified adapter's position represents a header.
         */
        boolean isHeader(int itemPosition);
    }
}
