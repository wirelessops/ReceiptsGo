package co.smartreceipts.android.utils.rx;

import com.hadisatrio.optional.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;
import java.util.Locale;

import co.smartreceipts.android.utils.TestLocaleToggler;
import io.reactivex.Observable;

@RunWith(RobolectricTestRunner.class)
public class PriceCharSequenceToBigDecimalObservableTransformerTest {

    @Before
    public void setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US);
    }

    @After
    public void tearDown() {
        TestLocaleToggler.resetDefaultLocale();
    }

    @Test
    public void composeTests() {
        Observable.just("")
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .test()
                .assertValue(Optional.absent())
                .assertNoErrors();

        Observable.just("abc")
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .test()
                .assertValue(Optional.absent())
                .assertNoErrors();

        Observable.just("10")
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .test()
                .assertValue(Optional.of(new BigDecimal("10")))
                .assertNoErrors();

        Observable.just("0.12")
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .test()
                .assertValue(Optional.of(new BigDecimal("0.12")))
                .assertNoErrors();

        TestLocaleToggler.setDefaultLocale(Locale.FRANCE);
        Observable.just("5,21")
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .test()
                .assertValue(Optional.of(new BigDecimal("5.21")))
                .assertNoErrors();
    }
}