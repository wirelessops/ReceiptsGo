package co.smartreceipts.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import androidx.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class SoftKeyboardManager {
    /**
     * After we resume Smart Receipts after using the camera, the input manager isn't quite ready yet to actually
     * show the keyboard for some reason (as opposed to using a text receipt). I wasn't able to find any checks
     * for why this fails, so I built in a hacky hardcoded delay to help resolve this
     */
    private static final int DELAY_TO_SHOW_KEYBOARD_MILLIS = 400;

    private SoftKeyboardManager() {
        //no instance
    }

    public static void showKeyboard(@Nullable final View view) {
        if (view == null) {
            return;
        }

        if (view.getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {

            final InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                // Try both immediately
                inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                view.postDelayed(() -> {
                    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT); // And delayed
                }, DELAY_TO_SHOW_KEYBOARD_MILLIS);
            }
        }

    }

    public static void hideKeyboard(@Nullable final View view) {
        if (view == null) {
            return;
        }

        final InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static class HideSoftKeyboardOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
                SoftKeyboardManager.hideKeyboard(view);
                return true;
            } else {
                return false;
            }
        }
    }
}
