package co.smartreceipts.android.persistence.database.tables.keys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.Category;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CategoryPrimaryKeyTest {

    // Class under test
    CategoryPrimaryKey mCategoryPrimaryKey;

    @Mock
    Category mCategory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mCategoryPrimaryKey = new CategoryPrimaryKey();
    }

    @Test
    public void getPrimaryKeyColumn() throws Exception {
        assertEquals("id", mCategoryPrimaryKey.getPrimaryKeyColumn());
    }

    @Test
    public void getPrimaryKeyClass() throws Exception {
        assertEquals(Integer.class, mCategoryPrimaryKey.getPrimaryKeyClass());
    }

    @Test
    public void getPrimaryKeyValue() throws Exception {
        final int id = 18;
        when(mCategory.getId()).thenReturn(id);
        assertEquals(id, (int) mCategoryPrimaryKey.getPrimaryKeyValue(mCategory));
    }
}