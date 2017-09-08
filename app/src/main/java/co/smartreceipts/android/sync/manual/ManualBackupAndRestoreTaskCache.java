package co.smartreceipts.android.sync.manual;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.PersistenceManager;

public class ManualBackupAndRestoreTaskCache {

    private final BackupTaskCacheHeadlessFragment mHeadlessFragment;
    private final PersistenceManager mPersistenceManager;
    private final Context mContext;

    public ManualBackupAndRestoreTaskCache(@NonNull FragmentManager fragmentManager, @NonNull PersistenceManager persistenceManager, @NonNull Context context) {
        Preconditions.checkNotNull(fragmentManager);
        mPersistenceManager = Preconditions.checkNotNull(persistenceManager);
        mContext = Preconditions.checkNotNull(context.getApplicationContext());

        BackupTaskCacheHeadlessFragment headlessFragment = (BackupTaskCacheHeadlessFragment) fragmentManager.findFragmentByTag(BackupTaskCacheHeadlessFragment.TAG);
        if (headlessFragment == null) {
            headlessFragment = new BackupTaskCacheHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, BackupTaskCacheHeadlessFragment.TAG).commit();
        }
        mHeadlessFragment = headlessFragment;
    }

    @NonNull
    public synchronized ManualBackupTask getManualBackupTask() {
        if (mHeadlessFragment.manualBackupTask == null) {
            mHeadlessFragment.manualBackupTask = new ManualBackupTask(mContext, mPersistenceManager);
        }
        return mHeadlessFragment.manualBackupTask;
    }

    @NonNull
    public synchronized ManualRestoreTask getManualRestoreTask() {
        if (mHeadlessFragment.manualRestoreTask == null) {
            mHeadlessFragment.manualRestoreTask = new ManualRestoreTask(mPersistenceManager, mContext);
        }
        return mHeadlessFragment.manualRestoreTask;
    }

    public static final class BackupTaskCacheHeadlessFragment extends Fragment {

        private static final String TAG = ManualBackupAndRestoreTaskCache.class.getName();

        private ManualBackupTask manualBackupTask;
        private ManualRestoreTask manualRestoreTask;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
