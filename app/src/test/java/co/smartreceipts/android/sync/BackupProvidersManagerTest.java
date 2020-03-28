package co.smartreceipts.android.sync;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.sync.network.SupportedNetworkType;
import co.smartreceipts.core.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.provider.SyncProviderFactory;
import co.smartreceipts.android.sync.provider.SyncProviderStore;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BackupProvidersManagerTest {

    // Class under test
    BackupProvidersManager mBackupProvidersManager;

    @Mock
    BackupProvider mNoneBackupProvider;

    @Mock
    BackupProvider mDriveBackupProvider;

    @Mock
    SyncProviderFactory mSyncProviderFactory;

    @Mock
    SyncProviderStore mSyncProviderStore;

    @Mock
    NetworkManager mNetworkManager;

    @Mock
    FragmentActivity mFragmentActivity;

    @Mock
    BackupProviderChangeListener mBackupProviderChangeListener1, mBackupProviderChangeListener2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mSyncProviderStore.getProvider()).thenReturn(SyncProvider.None);
        when(mSyncProviderFactory.get(SyncProvider.None)).thenReturn(mNoneBackupProvider);
        when(mSyncProviderFactory.get(SyncProvider.GoogleDrive)).thenReturn(mDriveBackupProvider);

        mBackupProvidersManager = new BackupProvidersManager(mSyncProviderFactory, mSyncProviderStore, mNetworkManager);
        mBackupProvidersManager.registerChangeListener(mBackupProviderChangeListener1);
        mBackupProvidersManager.registerChangeListener(mBackupProviderChangeListener2);
        mBackupProvidersManager.unregisterChangeListener(mBackupProviderChangeListener2);
    }

    @Test
    public void initialize() {
        mBackupProvidersManager.initialize(mFragmentActivity);
        verify(mNoneBackupProvider).initialize(mFragmentActivity);
    }

    @Test
    public synchronized void deinitialize() {
        mBackupProvidersManager.deinitialize();
        verify(mNoneBackupProvider).deinitialize();
    }

    @Test
    public void onActivityResult() {
        mBackupProvidersManager.onActivityResult(0, 0, null);
        verify(mNoneBackupProvider).onActivityResult(0, 0, null);
    }

    @Test
    public void getRemoteBackups() {
        mBackupProvidersManager.getRemoteBackups();
        verify(mNoneBackupProvider).getRemoteBackups();
    }

    @Test
    public void getDeviceSyncId() {
        mBackupProvidersManager.getDeviceSyncId();
        verify(mNoneBackupProvider).getDeviceSyncId();
    }

    @Test
    public void getSyncProvider() {
        assertEquals(SyncProvider.None, mBackupProvidersManager.getSyncProvider());
        verify(mSyncProviderStore, times(2)).getProvider();
    }

    @Test
    public void setAndInitializeNetworkProviderType() {
        mBackupProvidersManager.setAndInitializeNetworkProviderType(SupportedNetworkType.WifiOnly);
        verify(mNetworkManager).setAndInitializeNetworkProviderType(SupportedNetworkType.WifiOnly);
    }

    @Test
    public void setAndInitializeSyncProvider() {
        when(mSyncProviderStore.setSyncProvider(any(SyncProvider.class))).thenReturn(true);
        mBackupProvidersManager.setAndInitializeSyncProvider(SyncProvider.GoogleDrive, mFragmentActivity);
        verify(mNoneBackupProvider).deinitialize();
        verify(mDriveBackupProvider).initialize(mFragmentActivity);

        // And confirm our internal refs now work
        mBackupProvidersManager.onActivityResult(0, 0, null);
        verify(mDriveBackupProvider).onActivityResult(0, 0, null);
        mBackupProvidersManager.deinitialize();
        verify(mDriveBackupProvider).deinitialize();
        mBackupProvidersManager.initialize(mFragmentActivity);
        verify(mDriveBackupProvider, times(2)).initialize(mFragmentActivity);
        verify(mBackupProviderChangeListener1).onProviderChanged(SyncProvider.GoogleDrive);
        mBackupProvidersManager.getRemoteBackups();
        verify(mDriveBackupProvider).getRemoteBackups();
        mBackupProvidersManager.getDeviceSyncId();
        verify(mDriveBackupProvider).getDeviceSyncId();
        verifyZeroInteractions(mBackupProviderChangeListener2);
    }

    @Test
    public void setAndInitializeTheCurrentSyncProvider() {
        when(mSyncProviderStore.setSyncProvider(any(SyncProvider.class))).thenReturn(false);
        mBackupProvidersManager.setAndInitializeSyncProvider(SyncProvider.GoogleDrive, mFragmentActivity);
        verify(mNoneBackupProvider, never()).deinitialize();
        verify(mDriveBackupProvider, never()).initialize(mFragmentActivity);
        verifyZeroInteractions(mBackupProviderChangeListener1);
        verifyZeroInteractions(mBackupProviderChangeListener2);
    }

}