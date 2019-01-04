package co.smartreceipts.android.versioning

import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintVersionUpgradedListener
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppVersionUpgradesListTest {

    private lateinit var upgradesList: AppVersionUpgradesList

    @Mock
    private lateinit var automaticBackupRecoveryHintVersionUpgradedListener: AutomaticBackupRecoveryHintVersionUpgradedListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        upgradesList = AppVersionUpgradesList(automaticBackupRecoveryHintVersionUpgradedListener)
    }

    @Test
    fun getUpgradeListeners() {
        assertEquals(listOf(automaticBackupRecoveryHintVersionUpgradedListener), upgradesList.getUpgradeListeners())
    }
}