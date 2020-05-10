package co.smartreceipts.android.model.impl

import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.currency.PriceCurrency
import co.smartreceipts.android.date.DisplayableDate
import co.smartreceipts.android.model.AutoCompleteMetadata
import co.smartreceipts.android.model.Price
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.core.sync.model.SyncState
import co.smartreceipts.android.utils.TestLocaleToggler
import co.smartreceipts.android.utils.testParcel
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.sql.Date
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class TripTest {

    companion object {

        private const val ID = 5
        private val TRIP_UUID = UUID.randomUUID()
        private const val NAME = "TripName"
        private val DIRECTORY = File(File(NAME).absolutePath)
        private val START_DATE = Date(1409703721000L)
        private val END_DATE = Date(1409783794000L)
        private val START_TIMEZONE = TimeZone.getTimeZone(TimeZone.getAvailableIDs()[0])
        private val END_TIMEZONE = TimeZone.getTimeZone(TimeZone.getAvailableIDs()[1])
        private val START_DISPLAYABLE_DATE = DisplayableDate(START_DATE, START_TIMEZONE)
        private val END_DISPLAYABLE_DATE = DisplayableDate(END_DATE, END_TIMEZONE)
        private const val COMMENT = "Comment"
        private const val COST_CENTER = "Cost Center"
        private val CURRENCY = PriceCurrency.getInstance("USD")
        private const val NAME_HIDDEN_AUTO_COMPLETE = false
        private const val COMMENT_HIDDEN_AUTO_COMPLETE = false
        private const val COST_CENTER_HIDDEN_AUTO_COMPLETE = false
    }

    // Class under test
    private lateinit var trip: Trip

    private lateinit var syncState: SyncState

    private var price: Price = PriceBuilderFactory().setPrice(0.0).setCurrency(CURRENCY).build()

    private var autoCompleteMetadata: AutoCompleteMetadata = AutoCompleteMetadata(isNameHiddenFromAutoComplete = NAME_HIDDEN_AUTO_COMPLETE, isCommentHiddenFromAutoComplete = COMMENT_HIDDEN_AUTO_COMPLETE, isLocationHiddenFromAutoComplete = false, isCostCenterHiddenFromAutoComplete = COST_CENTER_HIDDEN_AUTO_COMPLETE)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        TestLocaleToggler.setDefaultLocale(Locale.US)
        syncState = DefaultObjects.newDefaultSyncState()
        trip = Trip(
            ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, COMMENT,
            COST_CENTER, syncState, price, price, autoCompleteMetadata
        )
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun getName() {
        assertEquals(NAME, trip.name)
    }

    @Test
    fun getUuid() {
        assertEquals(TRIP_UUID, trip.uuid)
    }

    @Test
    fun getDirectory() {
        assertEquals(DIRECTORY, trip.directory)
    }

    @Test
    fun getDirectoryPath() {
        assertEquals(DIRECTORY.absolutePath, trip.directoryPath)
    }

    @Test
    fun getStartDate() {
        assertEquals(START_DATE, trip.startDate)
    }

    @Test
    fun getStartTimeZone() {
        assertEquals(START_TIMEZONE, trip.startTimeZone)
    }

    @Test
    fun getEndDate() {
        assertEquals(END_DATE, trip.endDate)
    }

    @Test
    fun getEndTimeZone() {
        assertEquals(END_TIMEZONE, trip.endTimeZone)
    }

    @Test
    fun getNameHiddenFromAutoComplete() {
        assertEquals(NAME_HIDDEN_AUTO_COMPLETE, trip.autoCompleteMetadata.isNameHiddenFromAutoComplete)
    }

    @Test
    fun getCommentHiddenFromAutoComplete() {
        assertEquals(COMMENT_HIDDEN_AUTO_COMPLETE, trip.autoCompleteMetadata.isCommentHiddenFromAutoComplete)
    }

    @Test
    fun getCostCenterHiddenFromAutoComplete() {
        assertEquals(COST_CENTER_HIDDEN_AUTO_COMPLETE, trip.autoCompleteMetadata.isCostCenterHiddenFromAutoComplete)
    }

    @Test
    fun isDateInsideTripBounds() {
        assertTrue(trip.isDateInsideTripBounds(START_DATE))
        assertTrue(trip.isDateInsideTripBounds(END_DATE))
        assertTrue(trip.isDateInsideTripBounds(Date(START_DATE.time + 10)))
        assertTrue(trip.isDateInsideTripBounds(Date(END_DATE.time - 10)))

        assertFalse(trip.isDateInsideTripBounds(Date(START_DATE.time - TimeUnit.DAYS.toMillis(2))))
        assertFalse(trip.isDateInsideTripBounds(Date(END_DATE.time + TimeUnit.DAYS.toMillis(2))))
    }

    @Test
    fun getPrice() {
        assertNotNull(trip.price)
        trip.price = price
        assertEquals(price, trip.price)
    }

    @Test
    fun getDailySubTotal() {
        assertNotNull(trip.dailySubTotal)
        trip.dailySubTotal = price
        assertEquals(price, trip.dailySubTotal)
    }

    @Test
    fun getTripCurrency() {
        assertEquals(CURRENCY, trip.tripCurrency)
    }

    @Test
    fun getDefaultCurrencyCode() {
        assertEquals(CURRENCY.currencyCode, trip.defaultCurrencyCode)
    }

    @Test
    fun getComment() {
        assertEquals(COMMENT, trip.comment)
    }

    @Test
    fun getCostCenter() {
        assertEquals(COST_CENTER, trip.costCenter)
    }

    @Test
    fun getSyncState() {
        assertEquals(syncState, trip.syncState)
    }

    @Test
    fun compareTo() {
        assertTrue(
            trip.compareTo(
                Trip(
                    ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, COMMENT, COST_CENTER, syncState, price, price, autoCompleteMetadata
                )
            ) == 0
        )
        assertTrue(
                trip > Trip(
                        ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, DisplayableDate(Date(END_DATE.time * 2), END_TIMEZONE), CURRENCY, COMMENT,
                        COST_CENTER, syncState, price, price, autoCompleteMetadata
                )
        )
        assertTrue(
                trip < Trip(
                        ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, DisplayableDate(Date(0), END_TIMEZONE), CURRENCY, COMMENT, COST_CENTER,
                        syncState, price, price, autoCompleteMetadata
                )
        )
    }

    @Test
    fun equals() {
        assertEquals(trip, trip)
        assertEquals(
            trip,
            Trip(
                ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, COMMENT, COST_CENTER,
                    syncState, price, price, autoCompleteMetadata
            )
        )
        assertThat(trip, not(equalTo(Any())))
        assertThat(trip, not(equalTo(mock(Trip::class.java))))
        assertThat(
            trip,
            not(
                equalTo(
                    Trip(
                        ID, TRIP_UUID, File(""), START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, COMMENT, COST_CENTER,
                            syncState, price, price, autoCompleteMetadata
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    Trip(
                        ID, TRIP_UUID, DIRECTORY, DisplayableDate(Date(System.currentTimeMillis()), START_TIMEZONE), END_DISPLAYABLE_DATE, CURRENCY,
                        COMMENT, COST_CENTER, syncState, price, price, autoCompleteMetadata
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    Trip(
                        ID, TRIP_UUID, DIRECTORY, DisplayableDate(START_DATE, TimeZone.getTimeZone(TimeZone.getAvailableIDs()[2])), END_DISPLAYABLE_DATE,
                        CURRENCY, COMMENT, COST_CENTER, syncState, price, price, autoCompleteMetadata
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    Trip(
                        ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, DisplayableDate(Date(System.currentTimeMillis()), END_TIMEZONE), CURRENCY,
                        COMMENT, COST_CENTER, syncState, price, price, autoCompleteMetadata
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    Trip(
                        ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, DisplayableDate(END_DATE, TimeZone.getTimeZone(TimeZone.getAvailableIDs()[2])),
                        CURRENCY, COMMENT, COST_CENTER, syncState, price, price, autoCompleteMetadata
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    Trip(
                        ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE,
                        PriceCurrency.getInstance("EUR"), COMMENT, COST_CENTER,
                            syncState, price, price, autoCompleteMetadata
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    Trip(
                        ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, "bad",
                        COST_CENTER, syncState, price, price, autoCompleteMetadata
                    )
                )
            )
        )
        assertThat(
            trip,
            not(
                equalTo(
                    Trip(
                        ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, COMMENT, "bad",
                            syncState, price, price, autoCompleteMetadata
                    )
                )
            )
        )

        // Special equals cases (source, price, and daily subtotal don't count):
        val tripWithPrice = Trip(
            ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, COMMENT,
            COST_CENTER, syncState, price, price, autoCompleteMetadata
        )

        val tripWithDailySubTotal = Trip(
            ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, COMMENT,
            COST_CENTER, syncState, price, price, autoCompleteMetadata
        )

        tripWithPrice.price = price
        tripWithDailySubTotal.dailySubTotal = price
        assertEquals(
            trip,
            Trip(
                ID, TRIP_UUID, DIRECTORY, START_DISPLAYABLE_DATE, END_DISPLAYABLE_DATE, CURRENCY, COMMENT, COST_CENTER,
                    syncState, price, price, autoCompleteMetadata
            )
        )
        assertEquals(trip, tripWithPrice)
        assertEquals(trip, tripWithDailySubTotal)
    }

    @Test
    fun parcelEquality() {
        val tripFromParcel = trip.testParcel()

        assertNotSame(trip, tripFromParcel)
        assertEquals(trip, tripFromParcel)
    }

}