package co.smartreceipts.android.workers.reports

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.core.os.ConfigurationCompat
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.core.utils.log.Logger
import wb.android.flex.Flex
import java.util.*
import javax.inject.Inject

@ApplicationScope
class ReportResourcesManager @Inject constructor(private var context: Context,
                                                 private val preferenceManager: UserPreferenceManager,
                                                 private val flex: Flex) {

    fun getLocalizedContext(): Context {
        val currentLocalizedContextLocale = ConfigurationCompat.getLocales(context.resources.configuration).get(0)

        val desiredLocale = Locale(preferenceManager[UserPreference.ReportOutput.PreferredReportLanguage])

        if (currentLocalizedContextLocale.language != desiredLocale.language) {
            Logger.info(this, "Altering the user preferred report language to: {}", desiredLocale.language)
            val conf = Configuration(context.resources.configuration)
            conf.setLocale(desiredLocale)
            context = context.createConfigurationContext(conf)
        }

        return context

    }

    fun getFlexString(@StringRes resId: Int): String {
        return flex.getString(getLocalizedContext(), resId)
    }
}
