package co.smartreceipts.android;

import android.support.annotation.VisibleForTesting;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import co.smartreceipts.android.model.Keyed;

@VisibleForTesting
public class KeyedObject implements Keyed {

        @Override
        public int getId() {
            return Keyed.MISSING_ID;
        }

        @NotNull
        @Override
        public UUID getUuid() {
            return Keyed.Companion.getMISSING_UUID();
        }
    }