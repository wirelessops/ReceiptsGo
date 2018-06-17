package co.smartreceipts.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.TypedValue;

import com.google.common.base.Preconditions;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;
import dagger.Lazy;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class UserPreferenceManager {

    public static final String PREFERENCES_FILE_NAME = "SmartReceiptsPrefFile";

    private final Context context;

    private final Lazy<SharedPreferences> preferences;
    private final Scheduler initializationScheduler;

    @Inject
    UserPreferenceManager(@NonNull Context context,
                          @NonNull @Named(PREFERENCES_FILE_NAME) Lazy<SharedPreferences> preferences) {
        this(context.getApplicationContext(), preferences, Schedulers.io());
    }

    @VisibleForTesting
    UserPreferenceManager(@NonNull Context context,
                          @NonNull Lazy<SharedPreferences> preferences,
                          @NonNull Scheduler initializationScheduler) {
        this.context = context;
        this.preferences = preferences;
        this.initializationScheduler = Preconditions.checkNotNull(initializationScheduler);
    }

    @SuppressLint("CheckResult")
    public void initialize() {
        Logger.info(UserPreferenceManager.this, "Initializing the UserPreferenceManager...");

        getUserPreferencesObservable()
                .subscribeOn(this.initializationScheduler)
                .subscribe(userPreferences -> {
                    for (final UserPreference<?> userPreference : userPreferences) {
                        final String preferenceName = name(userPreference);
                        if (!preferences.get().contains(preferenceName)) {
                            // In here - we assign values that don't allow for preference_defaults.xml definitions (e.g. Locale Based Settings)
                            // Additionally, we set all float fields, which don't don't allow for 'android:defaultValue' settings
                            if (UserPreference.General.DateSeparator.equals(userPreference)) {
                                final String assignedDateSeparator = context.getString(UserPreference.General.DateSeparator.getDefaultValue());
                                if (TextUtils.isEmpty(assignedDateSeparator)) {
                                    final String localeDefaultDateSeparator = DateUtils.getDateSeparator(context);
                                    preferences.get().edit().putString(preferenceName, localeDefaultDateSeparator).apply();
                                    Logger.debug(UserPreferenceManager.this, "Assigned locale default date separator {}", localeDefaultDateSeparator);
                                }
                            } else if (UserPreference.General.DefaultCurrency.equals(userPreference)) {
                                final String assignedCurrencyCode = context.getString(UserPreference.General.DefaultCurrency.getDefaultValue());
                                if (TextUtils.isEmpty(assignedCurrencyCode)) {
                                    try {
                                        final String currencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
                                        preferences.get().edit().putString(preferenceName, currencyCode).apply();
                                        Logger.debug(UserPreferenceManager.this, "Assigned locale default currency code {}", currencyCode);
                                    } catch (IllegalArgumentException e) {
                                        preferences.get().edit().putString(preferenceName, "USD").apply();
                                        Logger.warn(UserPreferenceManager.this, "Failed to find this Locale's currency code. Defaulting to USD", e);
                                    }
                                }
                            } else if (UserPreference.Receipts.MinimumReceiptPrice.equals(userPreference)) {
                                final TypedValue typedValue = new TypedValue();
                                context.getResources().getValue(userPreference.getDefaultValue(), typedValue, true);
                                if (typedValue.getFloat() < 0) {
                                    final float defaultMinimumReceiptPrice = -Float.MAX_VALUE;
                                    preferences.get().edit().putFloat(preferenceName, defaultMinimumReceiptPrice).apply();
                                    Logger.debug(UserPreferenceManager.this, "Assigned default float value for {} as {}", preferenceName, defaultMinimumReceiptPrice);
                                }
                            } else if (Float.class.equals(userPreference.getType())) {
                                final TypedValue typedValue = new TypedValue();
                                context.getResources().getValue(userPreference.getDefaultValue(), typedValue, true);
                                preferences.get().edit().putFloat(preferenceName, typedValue.getFloat()).apply();
                                Logger.debug(UserPreferenceManager.this, "Assigned default float value for {} as {}", preferenceName, typedValue.getFloat());
                            }
                        }
                    }
                    Logger.debug(UserPreferenceManager.this, "Completed user preference initialization");
                });
    }

    @NonNull
    public <T> Observable<T> getObservable(final UserPreference<T> preference) {
        return Observable.fromCallable(() -> get(preference));
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T get(UserPreference<T> preference) {
        final String name = context.getString(preference.getName());
        if (Boolean.class.equals(preference.getType())) {
            return (T) Boolean.valueOf(preferences.get().getBoolean(name, context.getResources().getBoolean(preference.getDefaultValue())));
        } else if (String.class.equals(preference.getType())) {
            return (T) preferences.get().getString(name, context.getString(preference.getDefaultValue()));
        } else if (Float.class.equals(preference.getType())) {
            final TypedValue typedValue = new TypedValue();
            context.getResources().getValue(preference.getDefaultValue(), typedValue, true);
            return (T) Float.valueOf(preferences.get().getFloat(name, typedValue.getFloat()));
        } else if (Integer.class.equals(preference.getType())) {
            return (T) Integer.valueOf(preferences.get().getInt(name, context.getResources().getInteger(preference.getDefaultValue())));
        }  else {
            throw new IllegalArgumentException("Unsupported preference type: " + preference.getType());
        }
    }

    @NonNull
    public <T> Observable<T> setObservable(final UserPreference<T> preference, final T t) {
        return Observable.create(emitter -> {
            set(preference, t);
            emitter.onNext(t);
            emitter.onComplete();
        });
    }

    public <T> void set(UserPreference<T> preference, T t) {
        final String name = context.getString(preference.getName());
        if (Boolean.class.equals(preference.getType())) {
            preferences.get().edit().putBoolean(name, (Boolean) t).apply();
        } else if (String.class.equals(preference.getType())) {
            preferences.get().edit().putString(name, (String) t).apply();
        } else if (Float.class.equals(preference.getType())) {
            preferences.get().edit().putFloat(name, (Float) t).apply();
        } else if (Integer.class.equals(preference.getType())) {
            preferences.get().edit().putInt(name, (Integer) t).apply();
        }  else {
            throw new IllegalArgumentException("Unsupported preference type: " + preference.getType());
        }
    }

    @NonNull
    public Observable<List<UserPreference<?>>> getUserPreferencesObservable() {
        return Observable.create(emitter -> {
            emitter.onNext(UserPreference.values());
            emitter.onComplete();
        });
    }

    @NonNull
    public String name(@NonNull UserPreference<?> preference) {
        return context.getString(preference.getName());
    }

    /**
     * @return the current {@link SharedPreferences} implementation. This is now deprecated, and users
     * should prefer the {@link #set(UserPreference, Object)} method instead to interact with this component
     */
    @NonNull
    @Deprecated
    public SharedPreferences getSharedPreferences() {
        return preferences.get();
    }

}
