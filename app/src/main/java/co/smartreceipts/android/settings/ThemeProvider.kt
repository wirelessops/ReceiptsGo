package co.smartreceipts.android.settings

import android.app.UiModeManager
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import co.smartreceipts.android.R
import java.security.InvalidParameterException
import javax.inject.Inject

class ThemeProvider @Inject constructor(private val context: Context) {

    fun getTheme(selectedTheme: String): Int =
        when (selectedTheme) {
            context.getString(R.string.pref_general_theme_default_entryValue) -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            context.getString(R.string.pref_general_theme_dark_entryValue) -> UiModeManager.MODE_NIGHT_YES
            context.getString(R.string.pref_general_theme_light_entryValue) -> UiModeManager.MODE_NIGHT_NO
            else -> throw InvalidParameterException("Theme not defined for $selectedTheme")
        }

}