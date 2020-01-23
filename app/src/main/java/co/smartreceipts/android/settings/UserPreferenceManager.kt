package co.smartreceipts.android.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.TypedValue
import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateUtils
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.analytics.log.Logger
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@ApplicationScope
class UserPreferenceManager constructor(private val context: Context,
                                        private val preferences: Lazy<SharedPreferences>,
                                        private val initializationScheduler: Scheduler) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val userPreferenceChangedPublishSubject = PublishSubject.create<UserPreference<*>>()

    @Inject
    constructor(context: Context,
                @Named(PREFERENCES_FILE_NAME) preferences: Lazy<SharedPreferences>) : this(context.applicationContext, preferences, Schedulers.io())

    companion object {
        const val PREFERENCES_FILE_NAME = "SmartReceiptsPrefFile"

        const val MIN_RECEIPT_PRICE : Float = -1000f
    }

    /**
     * Initializes the user preferences with a sensible set of defaults
     */
    @SuppressLint("CheckResult")
    fun initialize() {
        Logger.info(this@UserPreferenceManager, "Initializing the UserPreferenceManager...")

        // Register a change listener to track whenever our shared preferences change
        Completable.fromAction { preferences.get().registerOnSharedPreferenceChangeListener(this) }
                .subscribeOn(this.initializationScheduler)
                .subscribe { Logger.debug(this, "Registered a shared preference change listener") }

        // Pre-initialize each of the preferences that require a more complete default value
        userPreferencesSingle
                .subscribeOn(this.initializationScheduler)
                .subscribe { userPreferences ->
                    for (userPreference in userPreferences) {
                        val preferenceName = name(userPreference)
                        if (!preferences.get().contains(preferenceName)) {
                            // In here - we assign values that don't allow for preference_defaults.xml definitions (e.g. Locale Based Settings)
                            // Additionally, we set all float fields, which don't don't allow for 'android:defaultValue' settings
                            if (UserPreference.General.DateSeparator == userPreference) {
                                val assignedDateSeparator = context.getString(UserPreference.General.DateSeparator.defaultValue)
                                if (TextUtils.isEmpty(assignedDateSeparator)) {
                                    val localeDefaultDateSeparator = DateUtils.getDateSeparator(context)
                                    preferences.get().edit().putString(preferenceName, localeDefaultDateSeparator).apply()
                                    Logger.debug(this@UserPreferenceManager, "Assigned locale default date separator {}", localeDefaultDateSeparator)
                                }
                            } else if (UserPreference.General.DefaultCurrency == userPreference) {
                                val assignedCurrencyCode = context.getString(UserPreference.General.DefaultCurrency.defaultValue)
                                if (TextUtils.isEmpty(assignedCurrencyCode)) {
                                    try {
                                        val currencyCode = Currency.getInstance(Locale.getDefault()).currencyCode
                                        preferences.get().edit().putString(preferenceName, currencyCode).apply()
                                        Logger.debug(this@UserPreferenceManager, "Assigned locale default currency code {}", currencyCode)
                                    } catch (e: IllegalArgumentException) {
                                        preferences.get().edit().putString(preferenceName, "USD").apply()
                                        Logger.warn(this@UserPreferenceManager, "Failed to find this Locale's currency code. Defaulting to USD", e)
                                    }

                                }
                            } else if (UserPreference.Receipts.MinimumReceiptPrice == userPreference) {
                                val typedValue = TypedValue()
                                context.resources.getValue(userPreference.defaultValue, typedValue, true)
                                if (typedValue.float < 0) {
                                    val defaultMinimumReceiptPrice = MIN_RECEIPT_PRICE
                                    preferences.get().edit().putFloat(preferenceName, defaultMinimumReceiptPrice).apply()
                                    Logger.debug(this@UserPreferenceManager, "Assigned default float value for {} as {}", preferenceName, defaultMinimumReceiptPrice)
                                }
                            } else if (Float::class.java == userPreference.type) {
                                val typedValue = TypedValue()
                                context.resources.getValue(userPreference.defaultValue, typedValue, true)
                                preferences.get().edit().putFloat(preferenceName, typedValue.float).apply()
                                Logger.debug(this@UserPreferenceManager, "Assigned default float value for {} as {}", preferenceName, typedValue.float)
                            } else if (UserPreference.ReportOutput.PreferredReportLanguage == userPreference) {
                                val currentLocale = Locale.getDefault()
                                val supportedLanguages = context.resources.getStringArray(R.array.pref_output_preferred_language_entryValues)
                                for (supportedLanguage in supportedLanguages) {
                                    if (currentLocale.language == supportedLanguage) {
                                        preferences.get().edit().putString(preferenceName, currentLocale.language).apply()
                                        break
                                    }
                                }
                            }
                        }
                    }
                    Logger.debug(this@UserPreferenceManager, "Completed user preference initialization")
                }
    }

    /**
     * Gets the value of a user preference
     *
     * @param preference a [UserPreference] to get the value of
     * @return an [Observable], which will emit the value contained within a specific [UserPreference]
     */
    fun <T> getObservable(preference: UserPreference<T>): Observable<T> {
        return Observable.fromCallable {
            get(preference)
        }
    }

    /**
     * Gets the value of a user preference
     *
     * @param preference a [UserPreference] to get the value of
     * @return an [Single], which will emit the value contained within a specific [UserPreference]
     */
    fun <T> getSingle(preference: UserPreference<T>): Single<T> {
        return Single.fromCallable {
            get(preference)
        }
    }

    /**
     * Gets the value of a user preference
     *
     * @param preference a [UserPreference] to get the value of
     * @return a value of type [T] for a the param
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(preference: UserPreference<T>): T {
        val name = context.getString(preference.name)
        return when {
            java.lang.Boolean::class.java == preference.type -> java.lang.Boolean.valueOf(preferences.get().getBoolean(name, context.resources.getBoolean(preference.defaultValue))) as T
            java.lang.String::class.java == preference.type -> preferences.get().getString(name, context.getString(preference.defaultValue)) as T
            java.lang.Float::class.java == preference.type -> {
                val typedValue = TypedValue()
                context.resources.getValue(preference.defaultValue, typedValue, true)
                java.lang.Float.valueOf(preferences.get().getFloat(name, typedValue.float)) as T
            }
            java.lang.Integer::class.java == preference.type -> Integer.valueOf(preferences.get().getInt(name, context.resources.getInteger(preference.defaultValue))) as T
            else -> throw IllegalArgumentException("Unsupported preference type: " + preference.type)
        }
    }

    /**
     * Sets the value of an [UserPreference] with a new value
     *
     * @param preference the [UserPreference] to update
     * @param t the new value of type [T] to set for this preference
     * @return an [Observable], which will emit the updated value on change
     */
    fun <T> setObservable(preference: UserPreference<T>, t: T): Observable<T> {
        return Observable.fromCallable {
            set(preference, t)
            return@fromCallable t
        }
    }

    /**
     * Sets the value of an [UserPreference] with a new value
     *
     * @param preference the [UserPreference] to update
     * @param t the new value of type [T] to set for this preference
     * @return an [Single], which will emit the updated value on change
     */
    fun <T> setSingle(preference: UserPreference<T>, t: T): Single<T> {
        return Single.fromCallable {
            set(preference, t)
            return@fromCallable t
        }
    }

    /**
     * Sets the value of an [UserPreference] with a new value
     *
     * @param preference the [UserPreference] to update
     * @param t the new value of type [T] to set for this preference
     * @return the new value of type [T]
     */
    operator fun <T> set(preference: UserPreference<T>, t: T) {
        val name = context.getString(preference.name)
        when {
            java.lang.Boolean::class.java == preference.type -> preferences.get().edit().putBoolean(name, t as Boolean).apply()
            java.lang.String::class.java == preference.type -> preferences.get().edit().putString(name, t as String).apply()
            java.lang.Float::class.java == preference.type -> preferences.get().edit().putFloat(name, t as Float).apply()
            java.lang.Integer::class.java == preference.type -> preferences.get().edit().putInt(name, t as Int).apply()
            else -> {
                Logger.warn(this@UserPreferenceManager, "Unsupported preference type: " + preference.type)
                throw IllegalArgumentException("Unsupported preference type: " + preference.type)
            }
        }
    }

    /**
     * Fetches the actual name (i.e. key) of a particular preference
     *
     * @param preference the [UserPreference] to that we want to find the desired key
     * @return the [String] key for this preference
     */
    fun name(preference: UserPreference<*>): String {
        return context.getString(preference.name)
    }

    @SuppressLint("CheckResult")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        userPreferencesSingle
                .subscribeOn(this.initializationScheduler)
                .subscribe { userPreferences ->
                    for (userPreference in userPreferences) {
                        val preferenceName = name(userPreference)
                        if (preferenceName == key) {
                            userPreferenceChangedPublishSubject.onNext(userPreference)
                            break
                        }
                    }
                }
    }

    /**
     * @return a [Single] that emits our entire [List] of [UserPreference]
     */
    val userPreferencesSingle: Single<List<UserPreference<*>>> get() = Single.fromCallable { UserPreference.values() }

    /**
     * @return an [Observable], which will emit a [UserPreference] value this key changes
     * behind the scenes to reflect a new value. It will never emit onComplete or onError
     */
    val userPreferenceChangeStream: Observable<UserPreference<*>> get() = userPreferenceChangedPublishSubject

    /**
     * @return the current [SharedPreferences] implementation. This is now deprecated, and users
     * should prefer the [.set] method instead to interact with this component
     */
    @Deprecated("We should prefer the UserPreferenceManager's getter/setter instead")
    val sharedPreferences: SharedPreferences get() = preferences.get()
}
