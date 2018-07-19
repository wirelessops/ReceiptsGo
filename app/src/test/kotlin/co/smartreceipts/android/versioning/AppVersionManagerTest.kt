package co.smartreceipts.android.versioning

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(RobolectricTestRunner::class)
class AppVersionManagerTest {

    companion object {
        private const val OLD_VERSION = 1
    }

    private lateinit var appVersionManager: AppVersionManager

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var packageManager: PackageManager

    @Mock
    private lateinit var userPreferenceManager: UserPreferenceManager

    @Mock
    private lateinit var appVersionUpgradesList: AppVersionUpgradesList

    @Mock
    private lateinit var versionUpgradedListener1: VersionUpgradedListener

    @Mock
    private lateinit var versionUpgradedListener2: VersionUpgradedListener

    private val packageInfo = PackageInfo()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(userPreferenceManager.getObservable(UserPreference.Internal.ApplicationVersionCode)).thenReturn(Observable.just(OLD_VERSION))
        whenever(context.packageManager).thenReturn(packageManager)
        whenever(context.packageName).thenReturn(RuntimeEnvironment.application.packageName)
        whenever(packageManager.getPackageInfo(anyString(), any())).thenReturn(packageInfo)
        whenever(appVersionUpgradesList.getUpgradeListeners()).thenReturn(Arrays.asList(versionUpgradedListener1, versionUpgradedListener2))
        appVersionManager = AppVersionManager(context, userPreferenceManager, appVersionUpgradesList, Schedulers.trampoline())
    }

    @Test
    fun onLaunchWithNewVersionThatIsTheSameAsTheOld() {
        val newVersion = OLD_VERSION
        packageInfo.versionCode = newVersion

        appVersionManager.onLaunch()
        verifyZeroInteractions(versionUpgradedListener1, versionUpgradedListener2)
        verify(userPreferenceManager, never()).set(UserPreference.Internal.ApplicationVersionCode, newVersion)
    }

    @Test
    fun onLaunchWithNewVersionThatIsAboveTheOld() {
        val newVersion = OLD_VERSION + 1
        packageInfo.versionCode = newVersion

        appVersionManager.onLaunch()
        verify(versionUpgradedListener1).onVersionUpgrade(OLD_VERSION, newVersion)
        verify(versionUpgradedListener2).onVersionUpgrade(OLD_VERSION, newVersion)
        verify(userPreferenceManager).set(UserPreference.Internal.ApplicationVersionCode, newVersion)
    }

    @Test
    fun onLaunchWithNewVersionThatThrowsException() {
        val newVersion = OLD_VERSION
        whenever(packageManager.getPackageInfo(anyString(), any())).thenThrow(PackageManager.NameNotFoundException("test"))

        appVersionManager.onLaunch()
        verifyZeroInteractions(versionUpgradedListener1, versionUpgradedListener2)
        verify(userPreferenceManager, never()).set(UserPreference.Internal.ApplicationVersionCode, newVersion)
    }

}