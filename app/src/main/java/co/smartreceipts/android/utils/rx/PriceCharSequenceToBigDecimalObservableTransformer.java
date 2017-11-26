package co.smartreceipts.android.utils.rx;

import android.support.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import java.math.BigDecimal;

import co.smartreceipts.android.model.utils.ModelUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Predicate;

/**
 * An {@link ObservableTransformer}, which is responsible for transforming a {@link CharSequence} that contains a
 * price value (eg "20.05") into an {@link Optional} that may contain a {@link BigDecimal}. Situations in which
 * the optional may be empty include: the text was blank, the price is invalid (eg "a"), etc.
 */
public class PriceCharSequenceToBigDecimalObservableTransformer implements ObservableTransformer<CharSequence, Optional<BigDecimal>> {

    @Override
    @NonNull
    public ObservableSource<Optional<BigDecimal>> apply(@NonNull Observable<CharSequence> upstream) {
        return upstream.map(CharSequence::toString)
                .flatMap(decimalText -> Observable.fromCallable(() -> Optional.of(ModelUtils.parseOrThrow(decimalText))).onErrorReturnItem(Optional.absent()));
    }
}
