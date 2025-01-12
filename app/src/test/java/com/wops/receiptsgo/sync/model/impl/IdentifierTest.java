package com.wops.receiptsgo.sync.model.impl;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.wops.core.sync.model.impl.Identifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class IdentifierTest {

    @Test
    public void getId() {
        final String id = "abcd";
        final Identifier identifier = new Identifier(id);
        assertEquals(id, identifier.getId());
    }

    @Test
    public void parcelEquality() {
        final String id = "abcd";
        final Identifier identifier = new Identifier(id);
        final Parcel parcel = Parcel.obtain();
        identifier.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final Identifier parceledID = Identifier.CREATOR.createFromParcel(parcel);
        assertNotNull(parceledID);
        assertEquals(identifier, parceledID);
    }

}