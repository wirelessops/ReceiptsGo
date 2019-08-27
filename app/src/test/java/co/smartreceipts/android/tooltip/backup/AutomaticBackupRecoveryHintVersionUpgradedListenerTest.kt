package co.smartreceipts.android.tooltip.backup

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutomaticBackupRecoveryHintVersionUpgradedListenerTest {

    private lateinit var listener: AutomaticBackupRecoveryHintVersionUpgradedListener

    @Mock
    private lateinit var store: AutomaticBackupRecoveryHintUserInteractionStore

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        listener = AutomaticBackupRecoveryHintVersionUpgradedListener(store)
    }

    @Test
    fun onVersionUpgradeFromExistingVersionInformsTheStoreThatAnInteractionHasOccurred() {
        listener.onVersionUpgrade(100, 200)
        verify(store).setUserHasInteractedWithAutomaticBackupRecoveryHint(true)
    }

    @Test
    fun onVersionUpgradeFromFreshInstallDoesNothing() {
        listener.onVersionUpgrade(-1, 200)
        verifyZeroInteractions(store)
    }
}