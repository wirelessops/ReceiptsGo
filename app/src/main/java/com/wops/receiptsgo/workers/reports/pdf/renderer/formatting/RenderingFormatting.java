package com.wops.receiptsgo.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class RenderingFormatting {

    private final Map<Class<? extends Formatting>, Formatting<?>> formattingMap = new HashMap<>();

    public void addFormatting(@NonNull Formatting<?> Formatting) {
        Preconditions.checkNotNull(Formatting);
        this.formattingMap.put(Formatting.getClass(), Formatting);
    }

    public boolean hasFormatting(@NonNull Class<? extends Formatting> type) {
        Preconditions.checkNotNull(type);
        return formattingMap.containsKey(type);
    }

    public void setFormatting(@NonNull RenderingFormatting renderingFormatting) {
        formattingMap.clear();
        formattingMap.putAll(renderingFormatting.formattingMap);
    }

    @Nullable
    public <T> T getFormatting(@NonNull Class<? extends Formatting<T>> type) {
        Preconditions.checkNotNull(type);
        final Formatting<?> Formatting = formattingMap.get(type);
        if (Formatting != null) {
            return (T) Formatting.value();
        } else {
            return null;
        }
    }

    @NonNull
    public <T> T getFormatting(@NonNull Class<? extends Formatting<T>> type, @NonNull T defaultValue) {
        Preconditions.checkNotNull(type);
        final Formatting<?> Formatting = formattingMap.get(type);
        if (Formatting != null) {
            return (T) Formatting.value();
        } else {
            return defaultValue;
        }
    }
}
