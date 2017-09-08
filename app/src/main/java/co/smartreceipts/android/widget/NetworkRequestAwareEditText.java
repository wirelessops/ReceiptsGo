package co.smartreceipts.android.widget;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import co.smartreceipts.android.R;

/**
 * <p>
 * Extends the default Android {@link EditText} behavior to allow for different network
 * states within this box via a right-aligned icon. Users of this class should maintain responsibility
 * for driving the different network states of this class via the following methods:
 * <ul>
 * <p/>
 * </ul>
 * </p>
 * <p>
 * Please note that this class overrides the <pre>@attr ref android.R.styleable#TextView_drawableEnd</pre>
 * attribute. Manually calling any of the setCompoundDrawable methods may break the behavior of this class.
 * </p>
 */
public class NetworkRequestAwareEditText extends AppCompatEditText {

    /**
     * Tracks the various network states this layout can exist within. All states must be driven externally
     */
    public enum State {

        /**
         * Initial/default state before another gets set
         */
        Unprepared,

        /**
         * Indicates that this view is ready to submit a network request
         */
        Ready,

        /**
         * Indicates that we are currently loading
         */
        Loading,

        /**
         * Indicates that we successfully performed a network request
         */
        Success,

        /**
         * Indicates that the network request failed
         */
        Failure
    }

    public interface RetryListener {

        /**
         * Callback method that is called whenever the user wish to retry the network request
         */
        void onUserRetry();
    }

    private State mState = State.Unprepared;
    private CharSequence mOriginalHint;
    private CharSequence mFailedHint;
    private RetryListener mRetryListener;
    private boolean mUserRetryActionEnabled;

    public NetworkRequestAwareEditText(Context context) {
        super(context);
        init();
    }

    public NetworkRequestAwareEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NetworkRequestAwareEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mOriginalHint = getHint();
        mFailedHint = getHint();
        mUserRetryActionEnabled = true;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final Drawable drawableEnd;
        if (!mUserRetryActionEnabled) {
            drawableEnd = null;
        }
        else {
            drawableEnd = getCompoundDrawablesRelative()[2];
        }

        if (drawableEnd != null) {
            final int start;
            if (ViewCompat.LAYOUT_DIRECTION_LTR == ViewCompat.getLayoutDirection(this)) {
                start = getWidth() - ViewCompat.getPaddingEnd(this) - drawableEnd.getIntrinsicWidth();
            } else {
                start = ViewCompat.getPaddingEnd(this);
            }

            final boolean wasTapped = event.getX() > start && event.getX() < start + drawableEnd.getIntrinsicWidth();
            if (wasTapped) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mRetryListener != null) {
                        mRetryListener.onUserRetry();
                    }
                    return true;
                } else {
                    return super.onTouchEvent(event);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * @return the current network state of this view
     */
    @NonNull
    public State getCurrentState() {
        return mState;
    }

    /**
     * Updates the current state of this view
     *
     * @param state the desired "new" state
     */
    public synchronized void setCurrentState(@NonNull State state) {
        if (mState == state) {
            // Exit early if nothing is changing
            return;
        }

        mState = state;
        final Drawable[] drawables;
        drawables = getCompoundDrawablesRelative();

        final Drawable drawableStart = drawables[0];
        final Drawable drawableTop = drawables[1];
        final Drawable drawableBottom = drawables[3];
        final Drawable drawableEnd;
        if (mUserRetryActionEnabled) {
            switch (state) {
                case Ready:
                    drawableEnd = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_refresh, getContext().getTheme());
                    setHint(mOriginalHint);
                    break;
                case Loading:
                    drawableEnd = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_refresh_in_progress, getContext().getTheme());
                    setHint(mOriginalHint);
                    break;
                case Failure:
                    drawableEnd = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_refresh, getContext().getTheme());
                    setHint(mFailedHint);
                    break;
                case Success:
                    drawableEnd = null;
                    setHint(mOriginalHint);
                    break;
                default:
                    drawableEnd = null;
                    setHint(mOriginalHint);
                    break;
            }
        } else {
            drawableEnd = null;
            setHint(mOriginalHint);
        }

        setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, drawableTop, drawableEnd, drawableBottom);

        if (drawableEnd instanceof Animatable) {
            ((Animatable)drawableEnd).start();
        }
    }

    public void setRetryListener(@Nullable RetryListener retryListener) {
        mRetryListener = retryListener;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (TextUtils.isEmpty(text)) {
            setCurrentState(State.Ready);
        } else {
            setCurrentState(State.Success);
        }
    }

    /**
     * @return the hint that appears if we enter the Failed state
     */
    public CharSequence getFailedHint() {
        return mFailedHint;
    }

    /**
     * Sets the hint that appears if we failed to complete the network request
     *
     * @param failedHint the new failed hint
     */
    public void setFailedHint(CharSequence failedHint) {
        mFailedHint = failedHint;
    }

    /**
     * Sets the hint that appears if we failed to complete the network request
     *
     * @param failedHint the new failed hint
     */
    public void setFailedHint(@StringRes int failedHint) {
        mFailedHint = getContext().getString(failedHint);
    }

    /**
     * Allows us to selectively enable/disable the ability for user's to retry
     *
     * @param enabled {@code true} if this behavior should be enabled. {@code false} otherwise
     */
    public void setUserRetryActionEnabled(boolean enabled) {
        mUserRetryActionEnabled = enabled;
        invalidate();
    }

    /**
     * @return {@code true} if user retry actions are enabled, {@code false} otherwise
     */
    public boolean isUserRetryActionEnabled() {
        return mUserRetryActionEnabled;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mState);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            mState = savedState.getState();
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * Utility class that allows us to persist {@link State} information
     * across config changes
     */
    static class SavedState extends BaseSavedState {

        private final State mState;

        public SavedState(@NonNull Parcelable superState, @NonNull State currentState) {
            super(superState);
            mState = currentState;
        }

        public SavedState(@NonNull Parcel in) {
            super(in);
            final State state = (State) in.readSerializable();
            mState = state != null ? state : State.Unprepared;
        }

        @NonNull
        public State getState() {
            return mState;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSerializable(mState);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(@NonNull Parcel in) {
                return new SavedState(in);
            }
            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
