package com.wops.receiptsgo.persistence.database.controllers.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.database.controllers.TableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class RefreshTripPricesListenerTest {
    
    // Class under test
    RefreshTripPricesListener<Object> mRefreshTripPricesListener;
    
    @Mock
    TableController<Trip> mTripTableController;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mRefreshTripPricesListener = new RefreshTripPricesListener<>(mTripTableController);
    }

    @Test
    public void onGetSuccess() {
        mRefreshTripPricesListener.onGetSuccess(Collections.emptyList());
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onGetFailure() {
        mRefreshTripPricesListener.onGetFailure(null);
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onInsertSuccess() {
        mRefreshTripPricesListener.onInsertSuccess(new Object(), new DatabaseOperationMetadata());
        verify(mTripTableController).get();
    }

    @Test
    public void onSyncInsertSuccess() {
        mRefreshTripPricesListener.onInsertSuccess(new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onInsertFailure() {
        mRefreshTripPricesListener.onInsertFailure(new Object(), null, new DatabaseOperationMetadata());
        mRefreshTripPricesListener.onInsertFailure(new Object(), null, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onUpdateSuccess() {
        mRefreshTripPricesListener.onUpdateSuccess(new Object(), new Object(), new DatabaseOperationMetadata());
        verify(mTripTableController).get();
    }

    @Test
    public void onSyncUpdateSuccess() {
        mRefreshTripPricesListener.onUpdateSuccess(new Object(), new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onUpdateFailure() {
        mRefreshTripPricesListener.onUpdateFailure(new Object(), null, new DatabaseOperationMetadata());
        mRefreshTripPricesListener.onUpdateFailure(new Object(), null, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onDeleteSuccess() {
        mRefreshTripPricesListener.onDeleteSuccess(new Object(), new DatabaseOperationMetadata());
        verify(mTripTableController).get();
    }

    @Test
    public void onSyncDeleteSuccess() {
        mRefreshTripPricesListener.onDeleteSuccess(new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verifyZeroInteractions(mTripTableController);
    }

    @Test
    public void onDeleteFailure() {
        mRefreshTripPricesListener.onDeleteFailure(new Object(), null, new DatabaseOperationMetadata());
        mRefreshTripPricesListener.onDeleteFailure(new Object(), null, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verifyZeroInteractions(mTripTableController);
    }

}