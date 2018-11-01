package co.smartreceipts.android.identity.widget

import android.os.Bundle
import android.support.v4.app.Fragment

abstract class NeededLoginFragment : Fragment(){

    companion object {
        const val OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN = "out_bool_was_previously_sent_to_login_screen"

    }

    var wasPreviouslySentToLogin = false


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NeededLoginFragment.OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN, wasPreviouslySentToLogin)
    }
}