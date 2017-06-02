package co.smartreceipts.android.widget.model;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class UiIndicatorTest {

    @Test
    public void idle() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.idle();
        assertEquals(UiIndicator.State.Idle, uiIndicator.getState());
        assertEquals(null, uiIndicator.getMessage().orNull());
    }

    @Test
    public void loading() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.loading();
        assertEquals(UiIndicator.State.Loading, uiIndicator.getState());
        assertEquals(null, uiIndicator.getMessage().orNull());
    }

    @Test
    public void error() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.error();
        assertEquals(UiIndicator.State.Error, uiIndicator.getState());
        assertEquals(null, uiIndicator.getMessage().orNull());
    }

    @Test
    public void errorWithMessage() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.error("test");
        assertEquals(UiIndicator.State.Error, uiIndicator.getState());
        assertEquals("test", uiIndicator.getMessage().orNull());
    }

    @Test
    public void success() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.success();
        assertEquals(UiIndicator.State.Success, uiIndicator.getState());
        assertEquals(null, uiIndicator.getMessage().orNull());
    }

    @Test
    public void successWithMessage() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.success("test");
        assertEquals(UiIndicator.State.Success, uiIndicator.getState());
        assertEquals("test", uiIndicator.getMessage().orNull());
    }

    @Test
    public void parcelEquality() {
        final UiIndicator errorIndicator = UiIndicator.error("test");
        final Parcel errorParcel = Parcel.obtain();
        errorIndicator.writeToParcel(errorParcel, 0);
        errorParcel.setDataPosition(0);
        assertEquals(errorIndicator, UiIndicator.CREATOR.createFromParcel(errorParcel));

        final UiIndicator idleIndicator = UiIndicator.idle();
        final Parcel idleParcel = Parcel.obtain();
        idleIndicator.writeToParcel(idleParcel, 0);
        idleParcel.setDataPosition(0);
        assertEquals(idleIndicator, UiIndicator.CREATOR.createFromParcel(idleParcel));
    }

}