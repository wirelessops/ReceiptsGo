package com.wops.core.identity.store;

import androidx.annotation.NonNull;

import com.wops.core.sync.model.impl.Identifier;

public class Token extends Identifier {

    public Token(@NonNull String token) {
        super(token);
    }
}
