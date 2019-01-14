package co.smartreceipts.android.versioning

import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintVersionUpgradedListener
import co.smartreceipts.android.tooltip.receipt.FirstReceiptQuestionsVersionUpgradedListener
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

    @Mock
    private lateinit var firstReceiptUseTaxesQuestionsVersionUpgradedListener: FirstReceiptQuestionsVersionUpgradedListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        upgradesList = AppVersionUpgradesList(
                automaticBackupRecoveryHintVersionUpgradedListener,
                firstReceiptUseTaxesQuestionsVersionUpgradedListener)
    }

    @Test
    fun getUpgradeListeners() {
        val expected = listOf(automaticBackupRecoveryHintVersionUpgradedListener, firstReceiptUseTaxesQuestionsVersionUpgradedListener)
        assertEquals(expected, upgradesList.getUpgradeListeners())
    }
}