package co.smartreceipts.android.imports.locator;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.hadisatrio.optional.Optional;

public class ActivityFileResultLocatorResponse {

    private final Optional<Throwable> throwable;

    private final Uri uri;
    private final int requestCode;
    private final int  resultCode;

    private ActivityFileResultLocatorResponse(Optional<Throwable> throwable, @Nullable Uri uri, int requestCode, int resultCode) {
        this.throwable = throwable;
        this.uri = uri;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
    }

    public static ActivityFileResultLocatorResponse LocatorError(Throwable throwable) {
        return new ActivityFileResultLocatorResponse(Optional.of(throwable), null, 0, 0);
    }

    public static ActivityFileResultLocatorResponse LocatorResponse(Uri uri, int requestCode, int resultCode) {
        return new ActivityFileResultLocatorResponse(Optional.absent(), uri, requestCode, resultCode);
    }

    public Optional<Throwable> getThrowable() {
        return throwable;
    }

    public Uri getUri() {
        return uri;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityFileResultLocatorResponse that = (ActivityFileResultLocatorResponse) o;

        if (requestCode != that.requestCode) return false;
        if (resultCode != that.resultCode) return false;
        if (!throwable.equals(that.throwable)) return false;
        return uri != null ? uri.equals(that.uri) : that.uri == null;

    }

    @Override
    public int hashCode() {
        int result = throwable.hashCode();
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + requestCode;
        result = 31 * result + resultCode;
        return result;
    }
}
