package wb.android.preferences

import android.view.View
import android.view.ViewGroup

class ViewStateChanger {

    var isEnabled = true

    fun enableView(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            val viewGroup = view
            for (i in 0 until viewGroup.childCount) {
                enableView(viewGroup.getChildAt(i), enabled)
            }
        }
    }
}