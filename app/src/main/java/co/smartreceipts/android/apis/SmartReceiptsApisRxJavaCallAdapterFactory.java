package co.smartreceipts.android.apis;

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


public class SmartReceiptsApisRxJavaCallAdapterFactory extends CallAdapter.Factory {

    private final CallAdapter.Factory original;

    private SmartReceiptsApisRxJavaCallAdapterFactory(@NonNull CallAdapter.Factory original) {
        this.original = original;
    }

    public static SmartReceiptsApisRxJavaCallAdapterFactory createWithScheduler(@NonNull Scheduler scheduler) {
        return new SmartReceiptsApisRxJavaCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(scheduler));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit));
    }

    private static class RxCallAdapterWrapper<R> implements CallAdapter<R, Observable<?>> {
        private final Retrofit retrofit;
        private final CallAdapter<R, Observable<?>> wrapped;

        public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<R, Observable<?>> wrapped) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Observable<?> adapt(Call<R> call) {
            return ((Observable) wrapped.adapt(call)).onErrorResumeNext((Object throwable) -> Observable.error(asPossiblyMappedException((Throwable) throwable))
            );
        }

        private Throwable asPossiblyMappedException(Throwable throwable) {
            // We had non-200 http error
            if (throwable instanceof HttpException) {
                final HttpException httpException = (HttpException) throwable;
                final Response response = httpException.response();
                if (response != null && response.errorBody() != null) {
                    // Only bother mapping if we saw an error response with actual content
                    return new SmartReceiptsApiException(response, httpException, retrofit);
                } else {
                    return throwable;
                }
            } else {
                // Network errors get returned as IO Exceptions, so pass those along
                return throwable;
            }
        }
    }
}

