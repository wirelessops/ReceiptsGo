package co.smartreceipts.android.imports.intents;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.model.IntentImportResult;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(RobolectricTestRunner.class)
public class IntentImportProcessorTest {

    IntentImportProcessor intentImportProcessor;
    IntentImportProcessor mockContentIntentImportProcessor;

    @Mock
    Analytics analytics;

    @Mock
    Context context;

    @Mock
    ContentResolver contentResolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(context.getApplicationContext()).thenReturn(context);
        when(context.getContentResolver()).thenReturn(contentResolver);

        intentImportProcessor = new IntentImportProcessor(RuntimeEnvironment.application, analytics);
        mockContentIntentImportProcessor = new IntentImportProcessor(context, analytics);
    }

    @Test
    public void processMainIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        final TestObserver<IntentImportResult> testObserver = intentImportProcessor.process(intent).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues()
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void processSendJpgImageIntent() {
        final Uri uri = Uri.fromFile(new File("/tmp/image.jpg"));
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        final IntentImportResult result = new IntentImportResult(uri, FileType.Image);
        final TestObserver<IntentImportResult> testObserver1 = intentImportProcessor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(result)
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.of(result));

        // But confirm subsequent attempts do nothing after marking consumed
        intentImportProcessor.markIntentAsSuccessfullyProcessed(intent);
        final TestObserver<IntentImportResult> testObserver2 = intentImportProcessor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertNoValues()
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.absent());
    }

    @Test
    public void processSendJpegImageIntent() {
        final Uri uri = Uri.fromFile(new File("/tmp/image.jpeg"));
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        final IntentImportResult result = new IntentImportResult(uri, FileType.Image);
        final TestObserver<IntentImportResult> testObserver1 = intentImportProcessor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(result)
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.of(result));

        // But confirm subsequent attempts do nothing after marking consumed
        intentImportProcessor.markIntentAsSuccessfullyProcessed(intent);
        final TestObserver<IntentImportResult> testObserver2 = intentImportProcessor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertNoValues()
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.absent());
    }

    @Test
    public void processSendPngImageIntent() {
        final Uri uri = Uri.fromFile(new File("/tmp/image.png"));
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        final IntentImportResult result = new IntentImportResult(uri, FileType.Image);
        final TestObserver<IntentImportResult> testObserver1 = intentImportProcessor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(result)
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.of(result));

        // But confirm subsequent attempts do nothing after marking consumed
        intentImportProcessor.markIntentAsSuccessfullyProcessed(intent);
        final TestObserver<IntentImportResult> testObserver2 = intentImportProcessor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertNoValues()
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.absent());
    }

    @Test
    public void processSendPdfFileIntent() {
        final Uri uri = Uri.fromFile(new File("/tmp/pdf.pdf"));
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        final IntentImportResult result = new IntentImportResult(uri, FileType.Pdf);
        final TestObserver<IntentImportResult> testObserver1 = intentImportProcessor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(result)
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.of(result));

        // But confirm subsequent attempts do nothing after marking consumed
        intentImportProcessor.markIntentAsSuccessfullyProcessed(intent);
        final TestObserver<IntentImportResult> testObserver2 = intentImportProcessor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertNoValues()
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.absent());
    }

    @Test
    public void processSendSmrFileIntent() {
        final Uri uri = Uri.fromFile(new File("/tmp/SmartReceipts.smr"));
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        final IntentImportResult result = new IntentImportResult(uri, FileType.Smr);
        final TestObserver<IntentImportResult> testObserver1 = intentImportProcessor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(result)
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.of(result));

        // But confirm subsequent attempts do nothing after marking consumed
        intentImportProcessor.markIntentAsSuccessfullyProcessed(intent);
        final TestObserver<IntentImportResult> testObserver2 = intentImportProcessor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertNoValues()
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.absent());
    }

    @Test
    public void processViewSmrFileIntent() {
        final Uri uri = Uri.fromFile(new File("/tmp/SmartReceipts.smr"));
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final IntentImportResult result = new IntentImportResult(uri, FileType.Smr);
        final TestObserver<IntentImportResult> testObserver1 = intentImportProcessor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(result)
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.of(result));

        // But confirm subsequent attempts do nothing after marking consumed
        intentImportProcessor.markIntentAsSuccessfullyProcessed(intent);
        final TestObserver<IntentImportResult> testObserver2 = intentImportProcessor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertNoValues()
                .assertComplete()
                .assertNoErrors();
        intentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.absent());
    }

    @Test
    public void processViewSmrContentIntent() {
        final Uri uri = Uri.parse("content://tmp/123456");
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final IntentImportResult result = new IntentImportResult(uri, FileType.Smr);
        when(contentResolver.getType(uri)).thenReturn("application/octet-stream");

        // Note: We use the mock context processor for this test
        final TestObserver<IntentImportResult> testObserver1 = mockContentIntentImportProcessor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValue(result)
                .assertComplete()
                .assertNoErrors();
        mockContentIntentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.of(result));

        // But confirm subsequent attempts do nothing after marking consumed
        mockContentIntentImportProcessor.markIntentAsSuccessfullyProcessed(intent);
        final TestObserver<IntentImportResult> testObserver2 = mockContentIntentImportProcessor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertNoValues()
                .assertComplete()
                .assertNoErrors();
        mockContentIntentImportProcessor.getLastResult()
                .test()
                .assertValue(Optional.absent());
    }

}