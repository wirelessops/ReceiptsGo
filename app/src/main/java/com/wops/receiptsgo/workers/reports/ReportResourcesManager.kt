package com.wops.receiptsgo.workers.reports

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.core.os.ConfigurationCompat
import co.smartreceipts.analytics.log.Logger
import com.wops.receiptsgo.R
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import co.smartreceipts.core.di.scopes.ApplicationScope
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

        if (currentLocalizedContextLocale?.language != desiredLocale.language) {
            Logger.info(
                this,
                "Altering the user preferred report language to: {}",
                desiredLocale.language
            )
            val conf = Configuration(context.resources.configuration)
            conf.setLocale(desiredLocale)
            context = context.createConfigurationContext(conf)
        }

        return context

    }

    fun getFlexString(@StringRes resId: Int): String {

        // Note: this hack is needed to override tax1 and tax2 column names to names that user set
        return when (resId) {
            R.string.pref_receipt_tax1_name_defaultValue -> {
                preferenceManager[UserPreference.Receipts.Tax1Name]
            }
            R.string.pref_receipt_tax2_name_defaultValue -> {
                preferenceManager[UserPreference.Receipts.Tax2Name]
            }
            else -> {
                flex.getString(getLocalizedContext(), resId)
            }
        }
    }
}
