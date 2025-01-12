package com.wops.receiptsgo.settings.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.text.DecimalFormat;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.Distance;
import wb.android.preferences.FloatSummaryEditTextPreference;

public class DefaultDistanceRatePreference extends FloatSummaryEditTextPreference {

	public DefaultDistanceRatePreference(Context context) {
		super(context);
	}

	public DefaultDistanceRatePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DefaultDistanceRatePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public CharSequence getSummary() {
		if (TextUtils.isEmpty(getText())) { //Add a zero check
			return getContext().getString(R.string.pref_distance_rate_summaryOff);
		}
		else {
			try {
				float value = Float.parseFloat(getText());
				if (value <= 0) {
                    return getContext().getString(R.string.pref_distance_rate_summaryOff);
				}
				DecimalFormat decimalFormat = new DecimalFormat();
				decimalFormat.setMaximumFractionDigits(Distance.RATE_PRECISION);
				decimalFormat.setMinimumFractionDigits(Distance.RATE_PRECISION);
				decimalFormat.setGroupingUsed(false);
				return getContext().getString(R.string.pref_distance_rate_summaryOn, decimalFormat.format(value));
			} catch (NumberFormatException e) {
				return getContext().getString(R.string.pref_distance_rate_summaryOff);
			}
		}
	}
}
