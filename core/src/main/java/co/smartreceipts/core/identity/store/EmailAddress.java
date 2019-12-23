package co.smartreceipts.core.identity.store;

import androidx.annotation.NonNull;

import co.smartreceipts.core.sync.model.impl.Identifier;

public class EmailAddress extends Identifier {

    public EmailAddress(@NonNull String email) {
        super(email);
    }
}
