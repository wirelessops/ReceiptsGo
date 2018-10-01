package co.smartreceipts.android.model.impl

import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.model.PaymentMethod
import co.smartreceipts.android.sync.model.SyncState
import co.smartreceipts.android.utils.testParcel
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotSame
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class PaymentMethodTest {

    companion object {

        private const val ID = 5
        private val PM_UUID = UUID.randomUUID()
        private const val METHOD = "method"
        private const val CUSTOM_ORDER_ID: Long = 2
    }

    // Class under test
    private lateinit var paymentMethod: PaymentMethod

    private lateinit var syncState: SyncState

    @Before
    fun setUp() {
        syncState = DefaultObjects.newDefaultSyncState()
        paymentMethod = PaymentMethod(ID, PM_UUID, METHOD, syncState, CUSTOM_ORDER_ID)
    }

    @Test
    fun getId() {
        assertEquals(ID.toLong(), paymentMethod.id.toLong())
    }

    @Test
    fun getMethod() {
        assertEquals(METHOD, paymentMethod.method)
    }

    @Test
    fun getSyncState() {
        assertEquals(syncState, paymentMethod.syncState)
    }

    @Test
    fun getCustomOrderId() {
        assertEquals(CUSTOM_ORDER_ID, paymentMethod.customOrderId)
    }

    @Test
    fun equals() {
        assertEquals(paymentMethod, paymentMethod)
        assertEquals(
            paymentMethod,
            PaymentMethod(ID, PM_UUID, METHOD, syncState, CUSTOM_ORDER_ID)
        )
        assertThat(paymentMethod, not(equalTo(Any())))
        assertThat(paymentMethod, not(equalTo(mock(PaymentMethod::class.java))))
        assertThat(paymentMethod,not(equalTo(PaymentMethod(-1, PM_UUID, METHOD, syncState, CUSTOM_ORDER_ID))))
        assertThat(
            paymentMethod,
            not(
                equalTo(
                    PaymentMethod(ID, PM_UUID, "abcd", syncState, CUSTOM_ORDER_ID)
                )
            )
        )
        assertThat(
            paymentMethod,
            not(
                equalTo(
                    PaymentMethod(ID, PM_UUID, "abcd", syncState, (CUSTOM_ORDER_ID + 1))
                )
            )
        )
        assertThat(
            paymentMethod,
            not(
                equalTo(
                    PaymentMethod(ID, UUID.randomUUID(), METHOD, syncState, (CUSTOM_ORDER_ID + 1))
                )
            )
        )
    }

    @Test
    fun parcelEquality() {
        val paymentMethodFromParcel = paymentMethod.testParcel()

        assertNotSame(paymentMethod, paymentMethodFromParcel)
        assertEquals(paymentMethod, paymentMethodFromParcel)
    }

    @Test
    fun compare() {
        val paymentMethod2 =
            PaymentMethod(ID, PM_UUID, METHOD, syncState, (CUSTOM_ORDER_ID + 1))
        val paymentMethod0 =
            PaymentMethod(ID, PM_UUID, METHOD, syncState, (CUSTOM_ORDER_ID - 1))

        val list = mutableListOf<PaymentMethod>().apply {
            add(paymentMethod)
            add(paymentMethod2)
            add(paymentMethod0)
            sort()
        }

        assertEquals(paymentMethod0, list[0])
        assertEquals(paymentMethod, list[1])
        assertEquals(paymentMethod2, list[2])
    }
}