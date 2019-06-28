package co.smartreceipts.android.widget.tooltip.report.intent;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.imports.intents.IntentImportProcessor;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.widget.IntentImportProvider;
import co.smartreceipts.android.widget.tooltip.TooltipManager;
import io.reactivex.Observable;

@ActivityScope
public class ImportInfoTooltipManager implements TooltipManager {

    private final IntentImportProcessor intentImportProcessor;
    private final IntentImportProvider intentImportProvider;

    @Inject
    public ImportInfoTooltipManager(@NonNull IntentImportProcessor intentImportProcessor, @NonNull IntentImportProvider intentImportProvider) {
        this.intentImportProcessor = Preconditions.checkNotNull(intentImportProcessor);
        this.intentImportProvider = Preconditions.checkNotNull(intentImportProvider);
    }

    public Observable<Boolean> needToShowImportInfo() {
        return intentImportProcessor.getLastResult()
                .map(intentImportResultOptional -> {
                    if (intentImportResultOptional.isPresent()) {
                        final FileType fileType = intentImportResultOptional.get().getFileType();
                        return fileType == FileType.Image || fileType == FileType.Pdf;
                    } else {
                        return false;
                    }
                });
    }

    @Override
    public void tooltipWasDismissed() {
        intentImportProvider.getIntentMaybe()
                .subscribe(intentImportProcessor::markIntentAsSuccessfullyProcessed);
    }
}
