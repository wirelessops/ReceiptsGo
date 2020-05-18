package wb.android.preferences;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;


public class DeactivatableCheckBoxPreference extends CheckBoxPreference implements DeactivatablePreference, Preference.OnPreferenceChangeListener {

    private OnPreferenceChangeListener mOnPreferenceChangeListener;

    private ViewStateChanger stateChanger = new ViewStateChanger();


    public DeactivatableCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
    }

    public DeactivatableCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
    }

    public DeactivatableCheckBoxPreference(Context context) {
        super(context);
        super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary(getSummary());
        if (mOnPreferenceChangeListener != null) {
            return mOnPreferenceChangeListener.onPreferenceChange(preference, newValue);
        }
        return true;
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        mOnPreferenceChangeListener = onPreferenceChangeListener;
    }

    @Override
    public void setAppearsEnabled(boolean appearsEnabled) {
        stateChanger.setEnabled(appearsEnabled);
    }

    @Override
    protected void onClick() {
        if (stateChanger.isEnabled()) {
            super.onClick();
        }
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        boolean viewEnabled = isEnabled() && stateChanger.isEnabled();
        stateChanger.enableView(view, viewEnabled);
    }
}
