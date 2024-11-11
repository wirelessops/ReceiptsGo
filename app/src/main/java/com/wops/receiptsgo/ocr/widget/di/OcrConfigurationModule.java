package com.wops.receiptsgo.ocr.widget.di;

import com.wops.receiptsgo.ocr.widget.configuration.OcrConfigurationFragment;
import com.wops.receiptsgo.ocr.widget.configuration.OcrConfigurationView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class OcrConfigurationModule {

    @Binds
    abstract OcrConfigurationView provideOcrConfigurationView(OcrConfigurationFragment fragment);

}
