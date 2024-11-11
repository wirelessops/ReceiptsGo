package com.wops.core.identity.store;

import androidx.annotation.NonNull;

import com.wops.core.sync.model.impl.Identifier;

public class UserId extends Identifier {

    public UserId(@NonNull String id) {
        super(id);
    }
}
