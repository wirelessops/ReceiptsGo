package wb.android.util;

import android.content.Context;
import android.widget.TextView;

public class UiUtils {

    public static void setTextAppearance(TextView textView, Context context, int resId) {
        if (Utils.ApiHelper.hasMarshmallow()) {
            textView.setTextAppearance(resId);
        } else {
            textView.setTextAppearance(context, resId);
        }

    }
}
