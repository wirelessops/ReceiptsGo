package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.DefaultObjects;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.sync.model.SyncState;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ImmutablePaymentMethodImplTest {

    private static final int ID = 5;
    private static final String METHOD = "method";
    private static final int CUSTOM_ORDER_ID = 2;

    // Class under test
    ImmutablePaymentMethodImpl mPaymentMethod;

    SyncState mSyncState;

    @Before
    public void setUp() throws Exception {
        mSyncState = DefaultObjects.newDefaultSyncState();
        mPaymentMethod = new ImmutablePaymentMethodImpl(ID, METHOD, mSyncState, CUSTOM_ORDER_ID);
    }

    @Test
    public void getId() {
        assertEquals(ID, mPaymentMethod.getId());
    }

    @Test
    public void getMethod() {
        assertEquals(METHOD, mPaymentMethod.getMethod());
    }

    @Test
    public void getSyncState() {
        assertEquals(mSyncState, mPaymentMethod.getSyncState());
    }

    @Test
    public void getCustomOrderId() {
        assertEquals(CUSTOM_ORDER_ID, mPaymentMethod.getCustomOrderId());
    }

    @Test
    public void equals() {
        assertEquals(mPaymentMethod, mPaymentMethod);
        assertEquals(mPaymentMethod, new ImmutablePaymentMethodImpl(ID, METHOD, mSyncState, CUSTOM_ORDER_ID));
        assertThat(mPaymentMethod, not(equalTo(new Object())));
        assertThat(mPaymentMethod, not(equalTo(mock(PaymentMethod.class))));
        assertThat(mPaymentMethod, not(equalTo(new ImmutablePaymentMethodImpl(-1, METHOD, mSyncState, CUSTOM_ORDER_ID))));
        assertThat(mPaymentMethod, not(equalTo(new ImmutablePaymentMethodImpl(ID, "abcd", mSyncState, CUSTOM_ORDER_ID))));
        assertThat(mPaymentMethod, not(equalTo(new ImmutablePaymentMethodImpl(ID, "abcd", mSyncState, CUSTOM_ORDER_ID + 1))));
    }

    @Test
    public void parcelEquality() {
        final Parcel parcel = Parcel.obtain();
        mPaymentMethod.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final ImmutablePaymentMethodImpl paymentMethod = ImmutablePaymentMethodImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(paymentMethod);
        assertEquals(paymentMethod, mPaymentMethod);
    }
}