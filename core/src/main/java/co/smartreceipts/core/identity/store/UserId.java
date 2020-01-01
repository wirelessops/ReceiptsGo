package co.smartreceipts.core.identity.store;

import androidx.annotation.NonNull;

import co.smartreceipts.core.sync.model.impl.Identifier;

public class UserId extends Identifier {

    public UserId(@NonNull String id) {
        super(id);
    }
}
