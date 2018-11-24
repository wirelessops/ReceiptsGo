package co.smartreceipts.android.identity.widget

import android.os.Bundle
import android.support.v4.app.Fragment

abstract class NeedsLoginFragment : Fragment() {

    companion object {
        const val OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN = "out_bool_was_previously_sent_to_login_screen"
    }

    protected var wasPreviouslySentToLogin: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            wasPreviouslySentToLogin =
                    savedInstanceState.getBoolean(NeedsLoginFragment.OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NeedsLoginFragment.OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN, wasPreviouslySentToLogin)
    }
}