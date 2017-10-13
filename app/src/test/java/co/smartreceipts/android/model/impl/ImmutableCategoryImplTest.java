package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.DefaultObjects;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.sync.model.SyncState;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ImmutableCategoryImplTest {

    private static final int ID = 3;
    private static final String NAME = "name";
    private static final String CODE = "code";
    private static final int CUSTOM_ORDER_ID = 15;
    // Class under test
    ImmutableCategoryImpl mImmutableCategory;

    SyncState mSyncState;

    @Before
    public void setUp() throws Exception {
        mSyncState = DefaultObjects.newDefaultSyncState();
        mImmutableCategory = new ImmutableCategoryImpl(ID, NAME, CODE, mSyncState, CUSTOM_ORDER_ID);
    }

    @Test
    public void getName() {
        assertEquals(NAME, mImmutableCategory.getName());
    }

    @Test
    public void getCode() {
        assertEquals(CODE, mImmutableCategory.getCode());
    }

    @Test
    public void getSyncState() {
        assertEquals(mSyncState, mImmutableCategory.getSyncState());
    }

    @Test
    public void getCustomOrderId() {
        assertEquals(CUSTOM_ORDER_ID, mImmutableCategory.getCustomOrderId());
    }

    @Test
    public void equals() {
        assertEquals(mImmutableCategory, mImmutableCategory);
        assertEquals(mImmutableCategory, new ImmutableCategoryImpl(ID, NAME, CODE, mSyncState, CUSTOM_ORDER_ID));
        assertThat(mImmutableCategory, not(equalTo(new Object())));
        assertThat(mImmutableCategory, not(equalTo(mock(Category.class))));
        assertThat(mImmutableCategory, not(equalTo(new ImmutableCategoryImpl(0, NAME, CODE, mSyncState, CUSTOM_ORDER_ID))));
        assertThat(mImmutableCategory, not(equalTo(new ImmutableCategoryImpl(ID, "wrong", CODE, mSyncState, CUSTOM_ORDER_ID))));
        assertThat(mImmutableCategory, not(equalTo(new ImmutableCategoryImpl(ID, NAME, "wrong", mSyncState, CUSTOM_ORDER_ID))));
        assertThat(mImmutableCategory, not(equalTo(new ImmutableCategoryImpl(ID, NAME, "wrong", mSyncState, CUSTOM_ORDER_ID + 1))));
    }

    @Test
    public void parcelEquality() {
        final Parcel parcel = Parcel.obtain();
        mImmutableCategory.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final ImmutableCategoryImpl category = ImmutableCategoryImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(category);
        assertEquals(category, mImmutableCategory);
    }

}