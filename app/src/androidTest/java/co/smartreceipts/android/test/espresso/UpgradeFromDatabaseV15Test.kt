package co.smartreceipts.android.test.espresso

import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import co.smartreceipts.android.test.runner.BeforeApplicationOnCreate
import co.smartreceipts.android.test.utils.TestResourceReader
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UpgradeFromDatabaseV15Test : UpgradeFromKnownDatabaseValidator() {

    companion object {

        @Suppress("unused")
        @JvmStatic
        @BeforeApplicationOnCreate
        fun setUpBeforeApplicationOnCreate() {
            UpgradeFromKnownDatabaseValidator.setUpBeforeApplicationOnCreate(TestResourceReader.V15_DATABASE)
        }

    }

}