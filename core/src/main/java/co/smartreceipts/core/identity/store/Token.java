package co.smartreceipts.core.identity.store;

import androidx.annotation.NonNull;

import co.smartreceipts.core.sync.model.impl.Identifier;

public class Token extends Identifier {

    public Token(@NonNull String token) {
        super(token);
    }
}
