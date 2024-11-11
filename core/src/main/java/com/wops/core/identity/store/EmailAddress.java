package com.wops.core.identity.store;

import androidx.annotation.NonNull;

import com.wops.core.sync.model.impl.Identifier;

public class EmailAddress extends Identifier {

    public EmailAddress(@NonNull String email) {
        super(email);
    }
}
