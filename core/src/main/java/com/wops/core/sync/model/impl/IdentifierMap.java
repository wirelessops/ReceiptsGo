package com.wops.core.sync.model.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.wops.core.sync.provider.SyncProvider;

public class IdentifierMap implements Serializable {

    @SerializedName("identifier_map")
    private final Map<SyncProvider, Identifier> mIdentifierMap;

    public IdentifierMap(@NonNull Map<SyncProvider, Identifier> identifierMap) {
        mIdentifierMap = new HashMap<>(Preconditions.checkNotNull(identifierMap));
    }

    @Nullable
    public Identifier getSyncId(@NonNull SyncProvider provider) {
        return mIdentifierMap.get(provider);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentifierMap)) return false;

        IdentifierMap that = (IdentifierMap) o;

        return mIdentifierMap.equals(that.mIdentifierMap);

    }

    @Override
    public int hashCode() {
        return mIdentifierMap.hashCode();
    }
}
