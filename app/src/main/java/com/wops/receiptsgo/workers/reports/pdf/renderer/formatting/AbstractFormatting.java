package com.wops.receiptsgo.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

abstract class AbstractFormatting<T> implements Formatting<T> {

    private final T value;
    private final Class<T> type;

    public AbstractFormatting(@NonNull T value, @NonNull Class<T> type) {
        this.value = Preconditions.checkNotNull(value);
        this.type = Preconditions.checkNotNull(type);
    }

    @NonNull
    @Override
    public T value() {
        return value;
    }

    @NonNull
    @Override
    public Class<T> getType() {
        return type;
    }
}
