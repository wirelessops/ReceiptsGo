package com.wops.receiptsgo.persistence.database.controllers.alterations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Completable;

import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class StubTableActionAlterationsTest {

    StubTableActionAlterations<Object> mStubTableActionAlterations;

    @Before
    public void setUp() throws Exception {
        mStubTableActionAlterations = new StubTableActionAlterations<>();
    }

    @Test
    public void preGet() throws Exception {
        final Completable completable = mStubTableActionAlterations.preGet();
        assertNotNull(completable);

        completable.test()
                .assertComplete()
                .assertNoErrors();
    }


    @Test
    public void postGet() {
        final List<Object> objects = Arrays.asList(new Object(), new Object(), new Object());
        mStubTableActionAlterations.postGet(objects)
                .test()
                .assertValue(objects)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void preInsert() {
        final Object object = new Object();
        mStubTableActionAlterations.preInsert(object)
                .test()
                .assertValue(object)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void postInsert() {
        final Object object = new Object();
        mStubTableActionAlterations.postInsert(object)
                .test()
                .assertValue(object)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void postInsertNull() {
        mStubTableActionAlterations.postInsert(null)
                .test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);
    }

    @Test
    public void preUpdate() {
        final Object oldObject = new Object();
        final Object object = new Object();

        mStubTableActionAlterations.preUpdate(oldObject, object)
                .test()
                .assertValue(object)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void postUpdate() {
        final Object oldObject = new Object();
        final Object object = new Object();


        mStubTableActionAlterations.postUpdate(oldObject, object)
                .test()
                .assertValue(object)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void postUpdateNull() {
        final Object oldObject = new Object();

        mStubTableActionAlterations.postUpdate(oldObject, null)
                .test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);
    }

    @Test
    public void preDelete() {
        final Object object = new Object();

        mStubTableActionAlterations.preDelete(object)
                .test()
                .assertValue(object)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void postDelete() {
        final Object object = new Object();

        mStubTableActionAlterations.postDelete(object)
                .test()
                .assertValue(object)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void postDeleteNull() {
        mStubTableActionAlterations.postDelete(null)
                .test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);
    }
}