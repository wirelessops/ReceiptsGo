package co.smartreceipts.android.imports.intents;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.model.IntentImportResult;
import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class IntentImportProcessor {

    private static final String INTENT_CONSUMED = "co.smartreceipts.android.INTENT_CONSUMED";

    private final Context context;
    private final Analytics analytics;

    private IntentImportResult lastResult;

    @Inject
    public IntentImportProcessor(@NonNull Context context, @NonNull Analytics analytics) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    /**
     * Processes an {@link Intent} to determine if this item contains an "attachment" (eg image file,
     * pdf, or SMR) that can be consumed by the app
     *
     * @param intent the desired {@link Intent}
     * @return a {@link Maybe} that can potentially contain an {@link IntentImportResult} with data
     * about how to handle the imported file
     */
    @NonNull
    public synchronized Maybe<IntentImportResult> process(@NonNull final Intent intent) {
        return Maybe.fromCallable(() -> {
                    if (intent.hasExtra(INTENT_CONSUMED)) {
                        return null; // We've already consumed this intent so do nothing
                    } else {
                        final Uri uri;
                        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
                            uri = intent.getData();
                        } else if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getExtras() != null) {
                            uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
                        } else {
                            uri = null; // This is an invalid one
                        }

                        if (uri != null) {
                            return buildResultFromUri(uri); // Attempt to build our results from it
                        } else {
                            return null;
                        }
                    }
                })
                .doOnSuccess(intentImportResult -> {
                    Logger.debug(IntentImportProcessor.this, "Successfully processed the file {} with uri: {}.", intentImportResult.getFileType(), intentImportResult.getUri());
                    analytics.record(new DefaultDataPointEvent(Events.Intents.ReceivedActionableIntent).addDataPoint(new DataPoint("type", intentImportResult.getFileType())));
                    lastResult = intentImportResult;
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Indicates that we've completed processing of this intent and should do no further work
     *
     * @param intent the desired {@link Intent}
     */
    public synchronized void markIntentAsSuccessfullyProcessed(@NonNull Intent intent) {
        intent.putExtra(INTENT_CONSUMED, true);
        lastResult = null;
    }

    /**
     * @return the last {@link IntentImportResult} that has passed through this stream.
     *
     * TODO: Refactor this once we've cleaned up the receipt list, so we can use Rx everywhere
     */
    @Nullable
    public synchronized IntentImportResult getLastResult() {
        return lastResult;
    }

    @Nullable
    private IntentImportResult buildResultFromUri(@NonNull Uri uri) {
        final String extension = UriUtils.getExtension(uri, context);
        final FileType fileType = FileType.getFileTypeFromExtension(extension);
        if (fileType != null) {
            return new IntentImportResult(uri, fileType);
        } else {
            return null;
        }
    }

}
