package co.smartreceipts.android.model.impl

import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.model.Category
import co.smartreceipts.android.sync.model.SyncState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImmutableCategoryImplTest {

    companion object {

        private const val ID = 3
        private const val NAME = "name"
        private const val CODE = "code"
        private const val CUSTOM_ORDER_ID = 15
    }

    // Class under test
    private lateinit var immutableCategory: ImmutableCategoryImpl

    private lateinit var syncState: SyncState

    @Before
    fun setUp() {
        syncState = DefaultObjects.newDefaultSyncState()
        immutableCategory =
                ImmutableCategoryImpl(ID, NAME, CODE, syncState, CUSTOM_ORDER_ID.toLong())
    }

    @Test
    fun getName() {
        assertEquals(NAME, immutableCategory.name)
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
        assertEquals(CUSTOM_ORDER_ID.toLong(), immutableCategory.customOrderId)
    }

    @Test
    fun equals() {
        assertEquals(immutableCategory, immutableCategory)
        assertEquals(
            immutableCategory,
            ImmutableCategoryImpl(ID, NAME, CODE, syncState, CUSTOM_ORDER_ID.toLong())
        )
        assertThat(immutableCategory, not(equalTo(Any())))
        assertThat(immutableCategory, not(equalTo(mock(Category::class.java))))
        assertThat(
            immutableCategory,
            not(equalTo(ImmutableCategoryImpl(0, NAME, CODE, syncState, CUSTOM_ORDER_ID.toLong())))
        )
        assertThat(
            immutableCategory,
            not(
                equalTo(
                    ImmutableCategoryImpl(
                        ID,
                        "wrong",
                        CODE,
                        syncState,
                        CUSTOM_ORDER_ID.toLong()
                    )
                )
            )
        )
        assertThat(
            immutableCategory,
            not(
                equalTo(
                    ImmutableCategoryImpl(
                        ID,
                        NAME,
                        "wrong",
                        syncState,
                        CUSTOM_ORDER_ID.toLong()
                    )
                )
            )
        )
        assertThat(
            immutableCategory,
            not(
                equalTo(
                    ImmutableCategoryImpl(
                        ID,
                        NAME,
                        "wrong",
                        syncState,
                        (CUSTOM_ORDER_ID + 1).toLong()
                    )
                )
            )
        )
    }

    @Test
    fun compare() {
        val category2 =
            ImmutableCategoryImpl(ID, NAME, CODE, syncState, (CUSTOM_ORDER_ID + 1).toLong())
        val category0 =
            ImmutableCategoryImpl(ID, NAME, CODE, syncState, (CUSTOM_ORDER_ID - 1).toLong())

        val list = mutableListOf<ImmutableCategoryImpl>().apply {
            add(immutableCategory)
            add(category2)
            add(category0)
            sort()
        }

        assertEquals(category0, list[0])
        assertEquals(immutableCategory, list[1])
        assertEquals(category2, list[2])
    }

}