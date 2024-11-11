package com.wops.receiptsgo.settings.catalog;

import android.content.Context;
import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.wops.receiptsgo.R;
import co.smartreceipts.analytics.log.Logger;

public final class UserPreference<T> {

    public static final class General {
        public static final UserPreference<String> Theme = new UserPreference<>(String.class, R.string.pref_general_theme_key, R.string.pref_general_theme_defaultValue);
        public static final UserPreference<Integer> DefaultReportDuration = new UserPreference<>(Integer.class, R.string.pref_general_trip_duration_key, R.integer.pref_general_trip_duration_defaultValue);
        public static final UserPreference<String> DefaultCurrency = new UserPreference<>(String.class, R.string.pref_general_default_currency_key, R.string.pref_general_default_currency_defaultValue);
        public static final UserPreference<String> DateSeparator = new UserPreference<>(String.class, R.string.pref_general_default_date_separator_key, R.string.pref_general_default_date_separator_defaultValue);
        public static final UserPreference<String> DateFormat = new UserPreference<>(String.class, R.string.pref_general_date_format_key, R.string.pref_general_date_format_defaultValue);
        public static final UserPreference<Boolean> IncludeCostCenter = new UserPreference<>(Boolean.class, R.string.pref_general_track_cost_center_key, R.bool.pref_general_track_cost_center_defaultValue);
        public static final UserPreference<Boolean> EnableCrop = new UserPreference<>(Boolean.class, R.string.pref_general_enable_crop_key, R.bool.pref_general_enable_crop_defaultValue);
    }

    public static final class Receipts {
        public static final UserPreference<Float> MinimumReceiptPrice = new UserPreference<>(Float.class, R.string.pref_receipt_minimum_receipts_price_key, R.dimen.pref_receipt_minimum_receipts_price_defaultValue);
        public static final UserPreference<Float> DefaultTaxPercentage = new UserPreference<>(Float.class, R.string.pref_receipt_tax_percent_key, R.dimen.pref_receipt_tax_percent_defaultValue);
        public static final UserPreference<Boolean> PredictCategories = new UserPreference<>(Boolean.class, R.string.pref_receipt_predict_categories_key, R.bool.pref_receipt_predict_categories_defaultValue);
        public static final UserPreference<Boolean> EnableAutoCompleteSuggestions = new UserPreference<>(Boolean.class, R.string.pref_receipt_enable_autocomplete_key, R.bool.pref_receipt_enable_autocomplete_defaultValue);
        public static final UserPreference<Boolean> OnlyIncludeReimbursable = new UserPreference<>(Boolean.class, R.string.pref_receipt_reimbursable_only_key, R.bool.pref_receipt_reimbursable_only_defaultValue);
        public static final UserPreference<Boolean> ReceiptsDefaultAsReimbursable = new UserPreference<>(Boolean.class, R.string.pref_receipt_reimbursable_default_key, R.bool.pref_receipt_reimbursable_default_defaultValue);
        public static final UserPreference<Boolean> ReceiptDateDefaultsToReportStartDate = new UserPreference<>(Boolean.class, R.string.pref_receipt_default_to_report_start_date_key, R.bool.pref_receipt_default_to_report_start_date_defaultValue);
        public static final UserPreference<Boolean> MatchReceiptNameToCategory = new UserPreference<>(Boolean.class, R.string.pref_receipt_match_name_to_category_key, R.bool.pref_receipt_match_name_to_category_defaultValue);
        public static final UserPreference<Boolean> MatchReceiptCommentToCategory = new UserPreference<>(Boolean.class, R.string.pref_receipt_match_comment_to_category_key, R.bool.pref_receipt_match_comment_to_category_defaultValue);
        public static final UserPreference<Boolean> ShowReceiptID = new UserPreference<>(Boolean.class, R.string.pref_receipt_show_id_key, R.bool.pref_receipt_show_id_defaultValue);
        public static final UserPreference<Boolean> IncludeTaxField = new UserPreference<>(Boolean.class, R.string.pref_receipt_include_tax_field_key, R.bool.pref_receipt_include_tax_field_defaultValue);
        public static final UserPreference<Boolean> IncludeTax2Field = new UserPreference<>(Boolean.class, R.string.pref_receipt_include_tax2_field_key, R.bool.pref_receipt_include_tax2_field_defaultValue);
        public static final UserPreference<Boolean> UsePreTaxPrice = new UserPreference<>(Boolean.class, R.string.pref_receipt_pre_tax_key, R.bool.pref_receipt_pre_tax_defaultValue);
        public static final UserPreference<Boolean> DefaultToFullPage = new UserPreference<>(Boolean.class, R.string.pref_receipt_full_page_key, R.bool.pref_receipt_full_page_defaultValue);
        public static final UserPreference<Boolean> UsePaymentMethods = new UserPreference<>(Boolean.class, R.string.pref_receipt_use_payment_methods_key, R.bool.pref_receipt_use_payment_methods_defaultValue);
        public static final UserPreference<String> Tax1Name = new UserPreference<>(String.class, R.string.pref_receipt_tax1_name_key, R.string.pref_receipt_tax1_name_defaultValue);
        public static final UserPreference<String> Tax2Name = new UserPreference<>(String.class, R.string.pref_receipt_tax2_name_key, R.string.pref_receipt_tax2_name_defaultValue);
        public static final UserPreference<Float> DefaultTax2Percentage = new UserPreference<>(Float.class, R.string.pref_receipt_tax2_percent_key, R.dimen.pref_receipt_tax2_percent_defaultValue);

    }

    public static final class ReportOutput {
        public static final UserPreference<String> UserId = new UserPreference<>(String.class, R.string.pref_output_username_key, R.string.pref_output_username_defaultValue);
        public static final UserPreference<Boolean> PrintUserIdByPdfPhoto = new UserPreference<>(Boolean.class, R.string.pref_output_print_receipt_id_by_photo_key, R.bool.pref_output_print_receipt_id_by_photo_defaultValue);
        public static final UserPreference<Boolean> PrintReceiptCommentByPdfPhoto = new UserPreference<>(Boolean.class, R.string.pref_output_print_receipt_comment_by_photo_key, R.bool.pref_output_print_receipt_comment_by_photo_defaultValue);
        public static final UserPreference<Boolean> ShowTotalOnCSV = new UserPreference<>(Boolean.class, R.string.pref_output_show_total_on_csv_key, R.bool.pref_output_show_total_on_csv_defaultValue);
        public static final UserPreference<Boolean> PrintReceiptsTableInLandscape = new UserPreference<>(Boolean.class, R.string.pref_output_receipts_landscape_key, R.bool.pref_output_receipts_landscape_defaultValue);
        public static final UserPreference<String> DefaultPdfPageSize = new UserPreference<>(String.class, R.string.pref_output_pdf_page_size_key, R.string.pref_output_pdf_page_size_defaultValue);
        public static final UserPreference<String> PreferredReportLanguage = new UserPreference<>(String.class, R.string.pref_output_preferred_language_key, R.string.pref_output_preferred_language_defaultValue);
    }

    public static final class Email {
        public static final UserPreference<String> ToAddresses = new UserPreference<>(String.class, R.string.pref_email_default_email_to_key, R.string.pref_email_default_email_to_defaultValue);
        public static final UserPreference<String> CcAddresses = new UserPreference<>(String.class, R.string.pref_email_default_email_cc_key, R.string.pref_email_default_email_cc_defaultValue);
        public static final UserPreference<String> BccAddresses = new UserPreference<>(String.class, R.string.pref_email_default_email_bcc_key, R.string.pref_email_default_email_bcc_defaultValue);
        public static final UserPreference<String> Subject = new UserPreference<>(String.class, R.string.pref_email_default_email_subject_key, R.string.EMAIL_DATA_SUBJECT);
    }

    public static final class Camera {
        public static final UserPreference<Boolean> SaveImagesInGrayScale = new UserPreference<>(Boolean.class, R.string.pref_camera_bw_key, R.bool.pref_camera_bw_defaultValue);
        public static final UserPreference<Boolean> AutomaticallyRotateImages = new UserPreference<>(Boolean.class, R.string.pref_camera_rotate_key, R.bool.pref_camera_rotate_defaultValue);
    }

    public static final class Layout {
        public static final UserPreference<Boolean> IncludeReceiptDateInLayout = new UserPreference<>(Boolean.class, R.string.pref_layout_display_date_key, R.bool.pref_layout_display_date_defaultValue);
        public static final UserPreference<Boolean> IncludeReceiptCategoryInLayout = new UserPreference<>(Boolean.class, R.string.pref_layout_display_category_key, R.bool.pref_layout_display_category_defaultValue);
    }

    public static final class Distance {
        public static final UserPreference<Float> DefaultDistanceRate = new UserPreference<>(Float.class, R.string.pref_distance_rate_key, R.dimen.pref_distance_rate_defaultValue);
        public static final UserPreference<Boolean> PrintDistanceTableInReports = new UserPreference<>(Boolean.class, R.string.pref_distance_print_table_key, R.bool.pref_distance_print_table_defaultValue);
        public static final UserPreference<Boolean> IncludeDistancePriceInReports = new UserPreference<>(Boolean.class, R.string.pref_distance_include_price_in_report_key, R.bool.pref_distance_include_price_in_report_defaultValue);
        public static final UserPreference<Boolean> PrintDistanceAsDailyReceiptInReports = new UserPreference<>(Boolean.class, R.string.pref_distance_print_daily_key, R.bool.pref_distance_print_daily_defaultValue);
        public static final UserPreference<Boolean> ShowDistanceAsPriceInSubtotal = new UserPreference<>(Boolean.class, R.string.pref_distance_as_price_key, R.bool.pref_distance_as_price_defaultValue);
    }

    public static final class PlusSubscription {
        public static final UserPreference<String> PdfFooterString = new UserPreference<>(String.class, R.string.pref_pro_pdf_footer_key, R.string.pref_pro_pdf_footer_defaultValue);
        public static final UserPreference<Boolean> SeparateByCategoryInReports = new UserPreference<>(Boolean.class, R.string.pref_pro_separate_by_category_key, R.bool.pref_pro_separate_by_category_defaultValue);
        public static final UserPreference<Boolean> CategoricalSummationInReports = new UserPreference<>(Boolean.class, R.string.pref_pro_categorical_summation_key, R.bool.pref_pro_categorical_summation_defaultValue);
        public static final UserPreference<Boolean> OmitDefaultTableInReports = new UserPreference<>(Boolean.class, R.string.pref_pro_omit_default_table_key, R.bool.pref_pro_omit_default_table_defaultValue);
    }

    public static final class Privacy {
        public static final UserPreference<Boolean> EnableAnalytics = new UserPreference<>(Boolean.class, R.string.pref_privacy_enable_analytics_key, R.bool.pref_privacy_enable_analytics_defaultValue);
        public static final UserPreference<Boolean> EnableCrashTracking = new UserPreference<>(Boolean.class, R.string.pref_privacy_enable_crash_tracking_key, R.bool.pref_privacy_enable_crash_tracking_defaultValue);
        public static final UserPreference<Boolean> EnableAdPersonalization = new UserPreference<>(Boolean.class, R.string.pref_privacy_enable_ad_personalization_key, R.bool.pref_privacy_enable_ad_personalization_defaultValue);
    }

    /**
     * These define preferences that aren't available in the settings menu but otherwise configurable by the user
     */
    public static final class Misc {
        public static final UserPreference<Boolean> AutoBackupOnWifiOnly = new UserPreference<>(Boolean.class, R.string.pref_no_category_auto_backup_wifi_only_key, R.bool.pref_no_category_auto_backup_wifi_only_defaultValue);
        public static final UserPreference<Boolean> OcrIncognitoMode = new UserPreference<>(Boolean.class, R.string.pref_no_category_ocr_incognito_mode_key, R.bool.pref_no_category_ocr_incognito_mode_defaultValue);
        public static final UserPreference<Boolean> OcrIsEnabled = new UserPreference<>(Boolean.class, R.string.pref_no_category_ocr_is_enabled_key, R.bool.pref_no_category_ocr_is_enabled_defaultValue);
    }


    /**
     * These define internal preferences that cannot be manually toggled by the user. Adding new fields here is highly discouraged in favor
     * of using {@link android.preference.PreferenceManager#getDefaultSharedPreferences(Context)} for internal preferences. This primarily
     * exists for backwards compatibility reasons
     */
    public static final class Internal {
        public static final UserPreference<Integer> ApplicationVersionCode = new UserPreference<>(Integer.class, R.string.pref_internal_app_version_code, R.integer.pref_internal_app_version_code_defaultValue);
    }

    private static List<UserPreference<?>> CACHED_VALUES;

    private final Class<T> type;
    private final int name;
    private final int defaultValue;

    private UserPreference(@NonNull Class<T> type, @StringRes int name, @AnyRes int defaultValue) {
        this.type = Preconditions.checkNotNull(type);
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @NonNull
    public Class<T> getType() {
        return type;
    }

    @StringRes
    public int getName() {
        return name;
    }

    @AnyRes
    public int getDefaultValue() {
        return defaultValue;
    }

    @NonNull
    public static synchronized List<UserPreference<?>> values() {
        if (CACHED_VALUES == null) {
            CACHED_VALUES = new ArrayList<>();
            final Class<?>[] declaredClasses = UserPreference.class.getDeclaredClasses();
            if (declaredClasses != null) {
                for (final Class<?> declaredClass : declaredClasses) {
                    final Field[] fields = declaredClass.getFields();
                    if (fields != null) {
                        for (final Field field : fields) {
                            if (UserPreference.class.equals(field.getType())) {
                                try {
                                    CACHED_VALUES.add((UserPreference<?>) field.get(declaredClass));
                                } catch (IllegalAccessException e) {
                                    Logger.warn(UserPreference.class, "Failed to get field " + field + " due to access exception", e);
                                }
                            }
                        }
                    }
                }
            }
        }
        return CACHED_VALUES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPreference)) return false;

        UserPreference<?> that = (UserPreference<?>) o;

        if (name != that.name) return false;
        if (defaultValue != that.defaultValue) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name;
        result = 31 * result + defaultValue;
        return result;
    }
}
