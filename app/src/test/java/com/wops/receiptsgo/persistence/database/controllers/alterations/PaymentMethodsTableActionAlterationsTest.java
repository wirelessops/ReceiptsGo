package com.wops.receiptsgo.persistence.database.controllers.alterations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class PaymentMethodsTableActionAlterationsTest {

    PaymentMethodsTableActionAlterations alterations;

    @Mock
    PaymentMethod paymentMethod1;

    @Mock
    PaymentMethod paymentMethod2;

    @Mock
    ReceiptsTable receiptsTable;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        alterations = new PaymentMethodsTableActionAlterations(receiptsTable);
    }

    @Test
    public void postUpdate() {
        alterations.postUpdate(paymentMethod1, paymentMethod2)
                .test()
                .assertValue(paymentMethod2)
                .assertComplete()
                .assertNoErrors();
        verify(receiptsTable).clearCache();
    }

    @Test
    public void postDelete() {
        alterations.postDelete(paymentMethod1)
                .test()
                .assertValue(paymentMethod1)
                .assertComplete()
                .assertNoErrors();
        verify(receiptsTable).clearCache();
    }
}