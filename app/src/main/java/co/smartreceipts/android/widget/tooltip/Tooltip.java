package co.smartreceipts.android.widget.tooltip;

import android.content.Context;
import android.support.annotation.NonNull;
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
import co.smartreceipts.android.tooltip.model.StaticTooltip;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class Tooltip extends RelativeLayout {

    private final PublishSubject<Object> tooltipClickStream = PublishSubject.create();
    private final PublishSubject<Object> buttonNoClickStream = PublishSubject.create();
    private final PublishSubject<Object> buttonYesClickStream = PublishSubject.create();
    private final PublishSubject<Object> buttonCancelClickStream = PublishSubject.create();
    private final PublishSubject<Object> closeIconClickStream = PublishSubject.create();

    private Button buttonNo, buttonYes, buttonCancel;
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
        inflate(getContext(), R.layout.app_tooltip, this);
        messageText = findViewById(R.id.tooltip_message);
        buttonNo = findViewById(R.id.tooltip_no);
        buttonYes = findViewById(R.id.tooltip_yes);
        buttonCancel = findViewById(R.id.tooltip_cancel);
        closeIcon = findViewById(R.id.tooltip_close_icon);
        errorIcon = findViewById(R.id.tooltip_error_icon);

        setVisibility(VISIBLE);
    }

    @NonNull
    public Observable<Object> getTooltipClickStream() {
        return tooltipClickStream;
    }

    @NonNull
    public Observable<Object> getButtonNoClickStream() {
        return buttonNoClickStream;
    }

    @NonNull
    public Observable<Object> getButtonYesClickStream() {
        return buttonYesClickStream;
    }

    @NonNull
    public Observable<Object> getButtonCancelClickStream() {
        return buttonCancelClickStream;
    }

    @NonNull
    public Observable<Object> getCloseIconClickStream() {
        return closeIconClickStream;
    }

    public void setTooltip(@NonNull StaticTooltip tooltip) {
        // Initially hide the "yes/no" question buttons
        buttonNo.setVisibility(GONE);
        buttonYes.setVisibility(GONE);

        switch (tooltip.getType()) {
            case Question:
                // Display these again if and only if a question
                buttonNo.setVisibility(VISIBLE);
                buttonYes.setVisibility(VISIBLE);
            case Informational:
                setInfoBackground();
                break;
            case Error:
                setErrorBackground();
                break;
        }

        // All tooltips must have a message (otherwise there's no reason to show them)
        messageText.setText(tooltip.getMessageResourceId());
        messageText.setVisibility(VISIBLE);

        if (tooltip.getShowWarningIcon()) {
            errorIcon.setVisibility(VISIBLE);
        } else {
            errorIcon.setVisibility(GONE);
        }

        // Tooltips may either have a close icon (ie 'X') or cancel button (but not both)
        if (tooltip.getShowCloseIcon()) {
            buttonCancel.setVisibility(GONE);
            closeIcon.setVisibility(VISIBLE);
        } else if (tooltip.getShowCancelButton()) {
            buttonCancel.setVisibility(VISIBLE);
            closeIcon.setVisibility(GONE);
        } else {
            buttonCancel.setVisibility(GONE);
            closeIcon.setVisibility(GONE);
        }

        // Configure all click streams
        setOnClickListener(v -> tooltipClickStream.onNext(new Object()));
        buttonNo.setOnClickListener(v -> buttonNoClickStream.onNext(new Object()));
        buttonYes.setOnClickListener(v -> buttonYesClickStream.onNext(new Object()));
        buttonCancel.setOnClickListener(v -> buttonCancelClickStream.onNext(new Object()));
        closeIcon.setOnClickListener(v -> closeIconClickStream.onNext(new Object()));
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

    public void setInfoWithCloseIcon(@StringRes int infoStringId, @Nullable OnClickListener tooltipClickListener,
                                     @Nullable OnClickListener closeClickListener, Object... formatArgs) {
        setInfoMessage(getContext().getString(infoStringId, formatArgs));
        setTooltipClickListener(tooltipClickListener);
        showCloseIcon(closeClickListener);

        errorIcon.setVisibility(VISIBLE);
        buttonNo.setVisibility(GONE);
        buttonYes.setVisibility(GONE);
        buttonCancel.setVisibility(GONE);
    }

    public void setInfo(@StringRes int infoStringId, @Nullable OnClickListener tooltipClickListener, @Nullable OnClickListener closeClickListener) {
        setInfoMessage(infoStringId);
        setTooltipClickListener(tooltipClickListener);
        showCloseIcon(closeClickListener);

        errorIcon.setVisibility(GONE);
        buttonNo.setVisibility(GONE);
        buttonYes.setVisibility(GONE);
        buttonCancel.setVisibility(GONE);
    }

    public void setQuestion(@StringRes int questionStringId, @Nullable OnClickListener noClickListener, @Nullable OnClickListener yesClickListener) {
        setInfoMessage(questionStringId);

        buttonNo.setVisibility(VISIBLE);
        buttonYes.setVisibility(VISIBLE);
        buttonCancel.setVisibility(GONE);

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

    public void showCancelButton(@NonNull OnClickListener cancelClickListener) {
        buttonCancel.setVisibility(VISIBLE);
        buttonCancel.setOnClickListener(cancelClickListener);
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
        buttonCancel.setVisibility(GONE);
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
