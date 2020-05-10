package co.smartreceipts.android.test.espresso

import androidx.test.filters.LargeTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.smartreceipts.android.test.runner.BeforeApplicationOnCreate
import co.smartreceipts.android.test.utils.TestResourceReader
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UpgradeFromDatabaseV19Test : UpgradeFromKnownDatabaseValidator() {

    companion object {

        @Suppress("unused")
        @JvmStatic
        @BeforeApplicationOnCreate
        fun setUpBeforeApplicationOnCreate() {
            setUpBeforeApplicationOnCreate(TestResourceReader.V19_DATABASE)
        }

    }

}