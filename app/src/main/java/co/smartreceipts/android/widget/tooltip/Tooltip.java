package co.smartreceipts.android.widget.tooltip;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.smartreceipts.android.R;

public class Tooltip extends RelativeLayout {

    private Button buttonNo, buttonYes;
    private TextView messageText;
    private ImageView closeIcon, errorIcon;

    public Tooltip(Context context) {
        super(context);
        init();
    }

    public Tooltip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Tooltip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.tooltip, this);
        messageText = (TextView) findViewById(R.id.tooltip_message);
        buttonNo = (Button) findViewById(R.id.tooltip_no);
        buttonYes = (Button) findViewById(R.id.tooltip_yes);
        closeIcon = (ImageView) findViewById(R.id.tooltip_close_icon);
        errorIcon = (ImageView) findViewById(R.id.tooltip_error_icon);

        setVisibility(VISIBLE);
    }

    public void setError(@StringRes int messageStringId, @Nullable OnClickListener closeClickListener) {
        setViewStateError();
        messageText.setText(getContext().getText(messageStringId));
        showCloseIcon(closeClickListener);
    }

    public void setErrorWithoutClose(@StringRes int messageStringId, @Nullable OnClickListener tooltipClickListener) {
        setViewStateError();
        closeIcon.setVisibility(GONE);

        messageText.setText(getContext().getText(messageStringId));
        setTooltipClickListener(tooltipClickListener);
    }

    public void setInfoWithIcon(@StringRes int infoStringId, @Nullable OnClickListener tooltipClickListener,
                        @Nullable OnClickListener closeClickListener, Object... formatArgs) {
        setInfoMessage(getContext().getString(infoStringId, formatArgs));
        setTooltipClickListener(tooltipClickListener);
        showCloseIcon(closeClickListener);

        errorIcon.setVisibility(VISIBLE);
        buttonNo.setVisibility(GONE);
        buttonYes.setVisibility(GONE);
    }

    public void setInfo(@StringRes int infoStringId, @Nullable OnClickListener tooltipClickListener, @Nullable OnClickListener closeClickListener) {
        setInfoMessage(infoStringId);
        setTooltipClickListener(tooltipClickListener);
        showCloseIcon(closeClickListener);

        errorIcon.setVisibility(GONE);
        buttonNo.setVisibility(GONE);
        buttonYes.setVisibility(GONE);
    }

    public void setQuestion(@StringRes int questionStringId, @Nullable OnClickListener noClickListener, @Nullable OnClickListener yesClickListener) {
        setInfoMessage(questionStringId);

        buttonNo.setVisibility(VISIBLE);
        buttonYes.setVisibility(VISIBLE);

        closeIcon.setVisibility(GONE);
        errorIcon.setVisibility(GONE);

        buttonNo.setOnClickListener(noClickListener);
        buttonYes.setOnClickListener(yesClickListener);
    }

    public void setInfoMessage(@StringRes int messageStringId) {
        setInfoBackground();
        messageText.setText(messageStringId);
        messageText.setVisibility(VISIBLE);
    }

    public void setInfoMessage(@Nullable CharSequence text) {
        setInfoBackground();
        messageText.setText(text);
        messageText.setVisibility(VISIBLE);
    }

    public void setTooltipClickListener(@Nullable OnClickListener tooltipClickListener) {
        setOnClickListener(tooltipClickListener);
    }

    public void showCloseIcon(@Nullable OnClickListener closeClickListener) {
        closeIcon.setVisibility(VISIBLE);
        closeIcon.setOnClickListener(closeClickListener);
    }

    private void setErrorBackground() {
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.smart_receipts_colorError));
    }

    private void setInfoBackground() {
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.smart_receipts_colorAccent));
    }

    private void setViewStateError() {
        setErrorBackground();

        messageText.setVisibility(VISIBLE);
        closeIcon.setVisibility(VISIBLE);
        errorIcon.setVisibility(VISIBLE);

        buttonNo.setVisibility(GONE);
        buttonYes.setVisibility(GONE);
    }

    public void hideWithAnimation() {
        if (getVisibility() != GONE) {
            TransitionManager.beginDelayedTransition((ViewGroup) getParent(), new AutoTransition());
            setVisibility(GONE);
        }
    }

    public void showWithAnimation() {
        if (getVisibility() != VISIBLE) {
            TransitionManager.beginDelayedTransition((ViewGroup) getParent(), new AutoTransition());
            setVisibility(VISIBLE);
        }
    }
}
