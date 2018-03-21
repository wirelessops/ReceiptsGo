package co.smartreceipts.android.widget.tooltip.report.intent;

import android.net.Uri;

import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.imports.intents.IntentImportProcessor;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.model.IntentImportResult;
import co.smartreceipts.android.imports.intents.widget.IntentImportProvider;
import io.reactivex.Observable;

import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ImportInfoTooltipManagerTest {

    private ImportInfoTooltipManager manager;

    @Mock
    IntentImportProcessor intentImportProcessor;

    @Mock
    IntentImportProvider intentImportProvider;

    private Uri fakeUri;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        fakeUri = Uri.EMPTY;

        manager = new ImportInfoTooltipManager(intentImportProcessor, intentImportProvider);
    }

    @Test
    public void notShowImportInfo() {
        when(intentImportProcessor.getLastResult()).thenReturn(Observable.just(Optional.absent()));

        manager.needToShowImportInfo()
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValueCount(1)
                .assertValue(false);
    }

    @Test
    public void showImportInfoForImage() {
        when(intentImportProcessor.getLastResult()).thenReturn(Observable.just(Optional.of(new IntentImportResult(fakeUri, FileType.Image))));

        manager.needToShowImportInfo()
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValueCount(1)
                .assertValue(true);
    }

    @Test
    public void showImportInfoForPdf() {
        when(intentImportProcessor.getLastResult()).thenReturn(Observable.just(Optional.of(new IntentImportResult(fakeUri, FileType.Pdf))));

        manager.needToShowImportInfo()
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValueCount(1)
                .assertValue(true);
    }

    @Test
    public void notShowImportInfoForBackup() {
        when(intentImportProcessor.getLastResult()).thenReturn(Observable.just(Optional.of(new IntentImportResult(fakeUri, FileType.Smr))));

        manager.needToShowImportInfo()
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValueCount(1)
                .assertValue(false);
    }
}
