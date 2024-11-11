package com.wops.receiptsgo.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;

public class Alignment extends AbstractFormatting<Alignment.Type> {

    public enum Type {
        Start, Centered
    }

    public Alignment(@NonNull Type alignment) {
        super(alignment, Type.class);
    }
}
