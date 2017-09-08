package co.smartreceipts.android.widget.model;

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
        assertEquals(null, uiIndicator.getData().orNull());
    }

    @Test
    public void loading() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.loading();
        assertEquals(UiIndicator.State.Loading, uiIndicator.getState());
        assertEquals(null, uiIndicator.getData().orNull());
    }

    @Test
    public void error() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.error();
        assertEquals(UiIndicator.State.Error, uiIndicator.getState());
        assertEquals(null, uiIndicator.getData().orNull());
    }

    @Test
    public void errorWithMessage() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.error("test");
        assertEquals(UiIndicator.State.Error, uiIndicator.getState());
        assertEquals("test", uiIndicator.getData().orNull());
    }

    @Test
    public void success() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.success();
        assertEquals(UiIndicator.State.Success, uiIndicator.getState());
        assertEquals(null, uiIndicator.getData().orNull());
    }

    @Test
    public void successWithMessage() throws Exception {
        final UiIndicator uiIndicator = UiIndicator.success("test");
        assertEquals(UiIndicator.State.Success, uiIndicator.getState());
        assertEquals("test", uiIndicator.getData().orNull());
    }

}