package com.wops.receiptsgo.widget.tooltip.report.intent;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.imports.intents.IntentImportProcessor;
import com.wops.receiptsgo.imports.intents.model.FileType;
import com.wops.receiptsgo.imports.intents.widget.IntentImportProvider;
import com.wops.receiptsgo.widget.tooltip.TooltipManager;
import com.wops.core.di.scopes.ActivityScope;
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
