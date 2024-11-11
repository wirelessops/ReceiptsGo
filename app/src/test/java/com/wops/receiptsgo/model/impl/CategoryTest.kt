package com.wops.receiptsgo.model.impl

import com.wops.receiptsgo.DefaultObjects
import com.wops.receiptsgo.model.Category
import co.smartreceipts.core.sync.model.SyncState
import com.wops.receiptsgo.utils.testParcel
import junit.framework.Assert
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class CategoryTest {

    companion object {

        private const val ID = 3
        private val CAT_UUID = UUID.randomUUID()
        private const val NAME = "name"
        private const val CODE = "code"
        private const val CUSTOM_ORDER_ID: Long = 15
    }

    // Class under test
    private lateinit var immutableCategory: Category

    private lateinit var syncState: SyncState

    @Before
    fun setUp() {
        syncState = DefaultObjects.newDefaultSyncState()
        immutableCategory =
                Category(ID, CAT_UUID, NAME, CODE, syncState, CUSTOM_ORDER_ID)
    }

    @Test
    fun getName() {
        assertEquals(NAME, immutableCategory.name)
    }

    @Test
    fun getUuid() {
        assertEquals(CAT_UUID, immutableCategory.uuid)
    }

    @Test
    fun getCode() {
        assertEquals(CODE, immutableCategory.code)
    }

    @Test
    fun getSyncState() {
        assertEquals(syncState, immutableCategory.syncState)
    }

    @Test
    fun getCustomOrderId() {
        assertEquals(CUSTOM_ORDER_ID, immutableCategory.customOrderId)
    }

    @Test
    fun equals() {
        assertEquals(immutableCategory, immutableCategory)
        assertEquals(
            immutableCategory,
            Category(ID, CAT_UUID, NAME, CODE, syncState, CUSTOM_ORDER_ID)
        )
        assertThat(immutableCategory, not(equalTo(Any())))
        assertThat(immutableCategory, not(equalTo(mock(Category::class.java))))
        assertThat(
            immutableCategory,
            not(equalTo(Category(0, CAT_UUID, NAME, CODE, syncState, CUSTOM_ORDER_ID)))
        )
        assertThat(
            immutableCategory,
            not(
                equalTo(
                    Category(ID, CAT_UUID, "wrong", CODE, syncState, CUSTOM_ORDER_ID)
                )
            )
        )
        assertThat(
            immutableCategory,
            not(
                equalTo(
                    Category(ID, CAT_UUID, NAME, "wrong", syncState, CUSTOM_ORDER_ID)
                )
            )
        )
        assertThat(
            immutableCategory,
            not(
                equalTo(
                    Category(ID, CAT_UUID, NAME, "wrong", syncState, (CUSTOM_ORDER_ID + 1))
                )
            )
        )
        assertThat(
            immutableCategory,
            not(equalTo(Category(ID, UUID.randomUUID(), NAME, CODE, syncState, CUSTOM_ORDER_ID)))
        )
    }

    @Test
    fun compare() {
        val category2 =
            Category(ID, CAT_UUID, NAME, CODE, syncState, (CUSTOM_ORDER_ID + 1))
        val category0 =
            Category(ID, CAT_UUID, NAME, CODE, syncState, (CUSTOM_ORDER_ID - 1))

        val list = mutableListOf<Category>().apply {
            add(immutableCategory)
            add(category2)
            add(category0)
            sort()
        }

        assertEquals(category0, list[0])
        assertEquals(immutableCategory, list[1])
        assertEquals(category2, list[2])
    }

    @Test
    fun parcelEquality() {
        val categoryFromParcel = immutableCategory.testParcel()

        junit.framework.Assert.assertNotSame(immutableCategory, categoryFromParcel)
        Assert.assertEquals(immutableCategory, categoryFromParcel)

    }

}