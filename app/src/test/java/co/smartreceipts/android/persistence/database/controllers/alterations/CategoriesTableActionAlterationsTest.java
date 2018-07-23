package co.smartreceipts.android.persistence.database.controllers.alterations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class CategoriesTableActionAlterationsTest {

    CategoriesTableActionAlterations alterations;

    @Mock
    Category category1;

    @Mock
    Category category2;

    @Mock
    ReceiptsTable receiptsTable;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        alterations = new CategoriesTableActionAlterations(receiptsTable);
    }

    @Test
    public void postUpdate() {
        alterations.postUpdate(category1, category2)
                .test()
                .assertValue(category2)
                .assertComplete()
                .assertNoErrors();
        verify(receiptsTable).clearCache();
    }

    @Test
    public void postDelete() {
        alterations.postDelete(category1)
                .test()
                .assertValue(category1)
                .assertComplete()
                .assertNoErrors();
        verify(receiptsTable).clearCache();
    }
}