package com.wops.receiptsgo.widget;

import android.view.View;
import android.widget.AdapterView;

/**
 * The default {@link AdapterView.OnItemSelectedListener} interface has a non-intuitive behavior
 * in that it always has an initial call (when the spinner is configured) before the user actually changes the
 * item (future call). We leverage this oddness to track the first call in this state in order that we might
 * remember any changes here.
 */
public abstract class UserSelectionTrackingOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    private int mPreviousPosition = -1;
    private boolean mIsFirstPass = true;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mIsFirstPass) {
            mIsFirstPass = false;
        } else if (position != mPreviousPosition) {
            onUserSelectedNewItem(parent, view, position, id, mPreviousPosition);
        }
        mPreviousPosition = position;
    }

    public abstract void onUserSelectedNewItem(AdapterView<?> parent, View view, int position, long id, int previousPosition);

}
