package com.wops.receiptsgo.persistence.database.controllers.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.database.controllers.TableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class ReceiptRefreshTripPricesListenerTest {

    // Class under test
    ReceiptRefreshTripPricesListener mReceiptRefreshTripPricesListener;

    @Mock
    TableController<Trip> mTripTableController;

    @Mock
    Receipt mReceipt;

    @Mock
    Trip mTrip;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mReceiptRefreshTripPricesListener = new ReceiptRefreshTripPricesListener(mTripTableController);
    }

    @Test
    public void onGetSuccess() {
        mReceiptRefreshTripPricesListener.onGetSuccess(Collections.<Receipt>emptyList(), mTrip);
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onGetFailure() {
        mReceiptRefreshTripPricesListener.onGetFailure(null, mTrip);
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onCopySuccess() {
        mReceiptRefreshTripPricesListener.onCopySuccess(mReceipt, mReceipt);
        verify(mTripTableController).get();
    }

    @Test
    public void onCopyFailure() {
        mReceiptRefreshTripPricesListener.onCopyFailure(mReceipt, null);
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onMoveSuccess() {
        mReceiptRefreshTripPricesListener.onMoveSuccess(mReceipt, mReceipt);
        verify(mTripTableController).get();
    }

    @Test
    public void onMoveFailure() {
        mReceiptRefreshTripPricesListener.onUpdateFailure(mReceipt, null, new DatabaseOperationMetadata());
        verifyZeroInteractions(mTripTableController);
    }
}