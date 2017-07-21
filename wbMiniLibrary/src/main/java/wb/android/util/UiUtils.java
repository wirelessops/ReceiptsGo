package wb.android.util;

import android.widget.TextView;

public class UiUtils {

    public static void setTextAppearance(TextView textView, int resId) {
        if (Utils.ApiHelper.hasMarshmallow()) {
            textView.setTextAppearance(resId);
        } else {
            textView.setTextAppearance(textView.getContext(), resId);
        }

    }
}
