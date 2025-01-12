package com.wops.receiptsgo.imports.intents;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.DataPoint;
import com.wops.analytics.events.DefaultDataPointEvent;
import com.wops.analytics.events.Events;
import com.wops.receiptsgo.imports.intents.model.FileType;
import com.wops.receiptsgo.imports.intents.model.IntentImportResult;
import com.wops.core.utils.UriUtils;
import com.wops.core.di.scopes.ApplicationScope;
import com.wops.analytics.log.Logger;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@ApplicationScope
public class IntentImportProcessor {

    private static final String INTENT_CONSUMED = "com.wops.receiptsgo.INTENT_CONSUMED";
    private static final Set<String> SUPPORTED_SMR_MIME_TYPES = new HashSet<>(Arrays.asList("application/octet-stream", "application/zip", "application/x-zip", "application/binary"));

    private final Context context;
    private final Analytics analytics;

    private Subject<Optional<IntentImportResult>> lastResult = BehaviorSubject.createDefault(Optional.absent());

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
                    lastResult.onNext(Optional.of(intentImportResult));
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
        lastResult.onNext(Optional.absent());
    }

    /**
     * @return the last {@link IntentImportResult} that has passed through this stream.
     *
     */
    public synchronized Observable<Optional<IntentImportResult>> getLastResult() {
        return lastResult;
    }

    @Nullable
    private IntentImportResult buildResultFromUri(@NonNull Uri uri) {
        FileType fileType = null;
        final String extension = UriUtils.getExtension(uri, context);
        if (!TextUtils.isEmpty(extension)) {
            fileType = FileType.getFileTypeFromExtension(extension);
        }
        if (fileType == null) {
            final String mimeType = UriUtils.getMimeType(uri, context);
            if (SUPPORTED_SMR_MIME_TYPES.contains(mimeType)) {
                fileType = FileType.Smr;
            }
        }

        if (fileType != null) {
            return new IntentImportResult(uri, fileType);
        } else {
            return null;
        }
    }

}
