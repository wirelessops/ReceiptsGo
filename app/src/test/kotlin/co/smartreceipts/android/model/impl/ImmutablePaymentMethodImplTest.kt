package co.smartreceipts.android.model.impl

import android.os.Parcel
import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.model.PaymentMethod
import co.smartreceipts.android.sync.model.SyncState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImmutablePaymentMethodImplTest {

    companion object {

        private const val ID = 5
        private const val METHOD = "method"
        private const val CUSTOM_ORDER_ID = 2
    }

    // Class under test
    private lateinit var paymentMethod: ImmutablePaymentMethodImpl

    private lateinit var syncState: SyncState

    @Before
    fun setUp() {
        syncState = DefaultObjects.newDefaultSyncState()
        paymentMethod =
                ImmutablePaymentMethodImpl(ID, METHOD, syncState, CUSTOM_ORDER_ID.toLong())
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
        assertEquals(CUSTOM_ORDER_ID.toLong(), paymentMethod.customOrderId)
    }

    @Test
    fun equals() {
        assertEquals(paymentMethod, paymentMethod)
        assertEquals(
            paymentMethod,
            ImmutablePaymentMethodImpl(ID, METHOD, syncState, CUSTOM_ORDER_ID.toLong())
        )
        assertThat(paymentMethod, not(equalTo(Any())))
        assertThat(paymentMethod, not(equalTo(mock(PaymentMethod::class.java))))
        assertThat(
            paymentMethod,
            not(
                equalTo(
                    ImmutablePaymentMethodImpl(
                        -1,
                        METHOD,
                        syncState,
                        CUSTOM_ORDER_ID.toLong()
                    )
                )
            )
        )
        assertThat(
            paymentMethod,
            not(
                equalTo(
                    ImmutablePaymentMethodImpl(
                        ID,
                        "abcd",
                        syncState,
                        CUSTOM_ORDER_ID.toLong()
                    )
                )
            )
        )
        assertThat(
            paymentMethod,
            not(
                equalTo(
                    ImmutablePaymentMethodImpl(
                        ID,
                        "abcd",
                        syncState,
                        (CUSTOM_ORDER_ID + 1).toLong()
                    )
                )
            )
        )
    }

    @Test
    fun parcelEquality() {
        val parcel = Parcel.obtain()
        paymentMethod.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val paymentMethod = ImmutablePaymentMethodImpl.CREATOR.createFromParcel(parcel)
        assertNotNull(paymentMethod)
        assertEquals(paymentMethod, this.paymentMethod)
    }

    @Test
    fun compare() {
        val paymentMethod2 =
            ImmutablePaymentMethodImpl(ID, METHOD, syncState, (CUSTOM_ORDER_ID + 1).toLong())
        val paymentMethod0 =
            ImmutablePaymentMethodImpl(ID, METHOD, syncState, (CUSTOM_ORDER_ID - 1).toLong())

        val list = mutableListOf<ImmutablePaymentMethodImpl>().apply {
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