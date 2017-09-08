package co.smartreceipts.android.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Locale;

import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.TestUtils;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class UserPreferenceManagerTest {

    // Class under test
    UserPreferenceManager userPreferenceManager;

    SharedPreferences preferences;

    Locale defaultLocale;

    @Before
    public void setUp() {
        defaultLocale = Locale.getDefault();
        preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        userPreferenceManager = new UserPreferenceManager(RuntimeEnvironment.application, preferences, Schedulers.trampoline());
    }

    @After
    public void tearDown() {
        Locale.setDefault(defaultLocale);
        preferences.edit().clear().apply();
    }

    @Test
    public void initialize() {
        userPreferenceManager.initialize();

        // Just confirm that we properly apply the default values for these special cases
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.General.DateSeparator.getName())));
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.General.DefaultCurrency.getName())));
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.Receipts.MinimumReceiptPrice.getName())));
        assertEquals(-Float.MAX_VALUE, preferences.getFloat(RuntimeEnvironment.application.getString(UserPreference.Receipts.MinimumReceiptPrice.getName()), 0), TestUtils.EPSILON);
    }

    @Test
    public void initializeForBadLocale() {
        Locale.setDefault(new Locale(""));
        userPreferenceManager.initialize();

        // Just confirm that we properly apply the default values for these special cases
        assertTrue(preferences.contains(RuntimeEnvironment.application.getString(UserPreference.General.DefaultCurrency.getName())));
        assertEquals("USD", preferences.getString(RuntimeEnvironment.application.getString(UserPreference.General.DefaultCurrency.getName()), null));
    }

    @Test
    public void getInteger() {
        final UserPreference<Integer> intPreference = UserPreference.General.DefaultReportDuration;
        final int intVal = RuntimeEnvironment.application.getResources().getInteger(intPreference.getDefaultValue());
        assertEquals(intVal, (long) userPreferenceManager.get(intPreference));

        userPreferenceManager.getObservable(intPreference)
                .test()
                .assertValue(intVal)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void getBoolean() {
        final UserPreference<Boolean> booleanPreference = UserPreference.Receipts.UsePaymentMethods;
        final boolean boolVal = RuntimeEnvironment.application.getResources().getBoolean(booleanPreference.getDefaultValue());
        assertEquals(boolVal, userPreferenceManager.get(booleanPreference));

        userPreferenceManager.getObservable(booleanPreference)
                .test()
                .assertValue(boolVal)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void getString() {
        final UserPreference<String> stringPreference = UserPreference.PlusSubscription.PdfFooterString;
        final String stringVal = RuntimeEnvironment.application.getString(stringPreference.getDefaultValue());
        assertEquals(stringVal, userPreferenceManager.get(stringPreference));

        userPreferenceManager.getObservable(stringPreference)
                .test()
                .assertValue(stringVal)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void getFloat() {
        final UserPreference<Float> floatPreference = UserPreference.Receipts.DefaultTaxPercentage;
        final TypedValue typedValue = new TypedValue();
        RuntimeEnvironment.application.getResources().getValue(floatPreference.getDefaultValue(), typedValue, true);
        final float floatVal = typedValue.getFloat();

        assertEquals(floatVal, userPreferenceManager.get(floatPreference), TestUtils.EPSILON);

        userPreferenceManager.getObservable(floatPreference)
                .test()
                .assertValue(floatVal)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void setInteger() {
        final UserPreference<Integer> intPreference = UserPreference.General.DefaultReportDuration;
        final int intVal = 999;

        userPreferenceManager.set(intPreference, 999);
        assertEquals(intVal, (long) userPreferenceManager.get(intPreference));

        userPreferenceManager.setObservable(intPreference, intVal)
                .test()
                .assertValue(intVal)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void setBoolean() {
        final UserPreference<Boolean> booleanPreference = UserPreference.Receipts.UsePaymentMethods;
        final boolean boolVal = true;

        userPreferenceManager.set(booleanPreference, boolVal);
        assertEquals(boolVal, userPreferenceManager.get(booleanPreference));

        userPreferenceManager.setObservable(booleanPreference, boolVal)
                .test()
                .assertValue(boolVal)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void setString() {
        final UserPreference<String> stringPreference = UserPreference.PlusSubscription.PdfFooterString;
        final String stringVal = "test";

        userPreferenceManager.set(stringPreference, stringVal);
        assertEquals(stringVal, userPreferenceManager.get(stringPreference));

        userPreferenceManager.setObservable(stringPreference, stringVal)
                .test()
                .assertValue(stringVal)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void setFloat() {
        final UserPreference<Float> floatPreference = UserPreference.Receipts.DefaultTaxPercentage;
        final float floatVal = 55.5f;

        userPreferenceManager.set(floatPreference, floatVal);
        assertEquals(floatVal, userPreferenceManager.get(floatPreference), TestUtils.EPSILON);

        userPreferenceManager.setObservable(floatPreference, floatVal)
                .test()
                .assertValue(floatVal)
                .assertComplete()
                .assertNoErrors();
    }

}