package co.smartreceipts.android.utils.rx;

import com.hadisatrio.optional.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import io.reactivex.Observable;

@RunWith(RobolectricTestRunner.class)
public class PriceCharSequenceToBigDecimalObservableTransformerTest {

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

        Observable.just("5,21")
                .compose(new PriceCharSequenceToBigDecimalObservableTransformer())
                .test()
                .assertValue(Optional.of(new BigDecimal("5.21")))
                .assertNoErrors();
    }
}