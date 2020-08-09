package co.smartreceipts.android.sync.widget.backups;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.core.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.core.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.FileUtils;
import co.smartreceipts.android.utils.cache.SmartReceiptsTemporaryFileCache;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import wb.android.storage.StorageManager;

class RemoteBackupsDataCache {

    private final Context mContext;
    private final BackupProvidersManager mBackupProvidersManager;
    private final NetworkManager mNetworkManager;
    private final DatabaseHelper mDatabaseHelper;
    private RemoteBackupsResultsCacheHeadlessFragment mHeadlessFragment;

    RemoteBackupsDataCache(@NonNull FragmentManager fragmentManager, @NonNull Context context,
                           @NonNull BackupProvidersManager backupProvidersManager, @NonNull NetworkManager networkManager,
                           @NonNull DatabaseHelper databaseHelper) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mBackupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        mNetworkManager = Preconditions.checkNotNull(networkManager);
        mDatabaseHelper = Preconditions.checkNotNull(databaseHelper);
        Preconditions.checkNotNull(fragmentManager);

        RemoteBackupsResultsCacheHeadlessFragment headlessFragment = (RemoteBackupsResultsCacheHeadlessFragment) fragmentManager.findFragmentByTag(RemoteBackupsResultsCacheHeadlessFragment.TAG);
        if (headlessFragment == null) {
            headlessFragment = new RemoteBackupsResultsCacheHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, RemoteBackupsResultsCacheHeadlessFragment.TAG).commit();
        }
        mHeadlessFragment = headlessFragment;
    }

    @NonNull
    synchronized Observable<List<RemoteBackupMetadata>> getBackups(@NonNull SyncProvider syncProvider) {
        if (mHeadlessFragment.getBackupsReplaySubjectMap == null) {
            mHeadlessFragment.getBackupsReplaySubjectMap = new HashMap<>();
        }
        ReplaySubject<List<RemoteBackupMetadata>> backupsReplaySubject = mHeadlessFragment.getBackupsReplaySubjectMap.get(syncProvider);
        if (backupsReplaySubject == null) {
            backupsReplaySubject = ReplaySubject.create();
            mBackupProvidersManager.getRemoteBackups()
                    .onErrorResumeNext(throwable -> mNetworkManager.getNetworkStateChangeObservable()
                            .filter(hasNetwork -> hasNetwork)
                            .firstOrError()
                            .flatMap(hasNetwork -> mBackupProvidersManager.getRemoteBackups()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .subscribe(backupsReplaySubject);

            mHeadlessFragment.getBackupsReplaySubjectMap.put(syncProvider, backupsReplaySubject);
        }
        return backupsReplaySubject;
    }

    synchronized void clearGetBackupsResults() {
        if (mHeadlessFragment.getBackupsReplaySubjectMap != null) {
            mHeadlessFragment.getBackupsReplaySubjectMap.clear();
        }
    }

    @NonNull
    synchronized Observable<com.google.api.services.drive.model.File> renameBackup(@NonNull RemoteBackupMetadata remoteBackupMetadata, @NonNull String newFileName) {
        if (mHeadlessFragment.renameBackupReplaySubjectMap == null) {
            mHeadlessFragment.renameBackupReplaySubjectMap = new HashMap<>();
        }
        ReplaySubject<com.google.api.services.drive.model.File> renameReplaySubject = mHeadlessFragment.renameBackupReplaySubjectMap.get(remoteBackupMetadata);
        if (renameReplaySubject == null) {
            renameReplaySubject = ReplaySubject.create();

            mBackupProvidersManager.renameBackup(remoteBackupMetadata, newFileName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .subscribe(renameReplaySubject);
            mHeadlessFragment.renameBackupReplaySubjectMap.put(remoteBackupMetadata, renameReplaySubject);
        }
        return renameReplaySubject;
    }

    @NonNull
    synchronized Observable<Boolean> deleteBackup(@Nullable RemoteBackupMetadata remoteBackupMetadata) {
        if (mHeadlessFragment.deleteBackupReplaySubjectMap == null) {
            mHeadlessFragment.deleteBackupReplaySubjectMap = new HashMap<>();
        }
        ReplaySubject<Boolean> deleteReplaySubject = mHeadlessFragment.deleteBackupReplaySubjectMap.get(remoteBackupMetadata);
        if (deleteReplaySubject == null) {
            deleteReplaySubject = ReplaySubject.create();
            getBackupMetadata(remoteBackupMetadata)
                    .flatMap(remoteBackupMetadataOptional -> deleteLocalSyncDataIfNeeded(remoteBackupMetadataOptional.isPresent() ? remoteBackupMetadataOptional.get() : null)
                            .flatMap(success -> {
                                if (success) {
                                    if (remoteBackupMetadata == null) {
                                        Logger.info(RemoteBackupsDataCache.this, "Clearing current configuration");
                                        return mBackupProvidersManager.clearCurrentBackupConfiguration();
                                    } else {
                                        Logger.info(RemoteBackupsDataCache.this, "Deleting provided metadata");
                                        return mBackupProvidersManager.deleteBackup(remoteBackupMetadata);
                                    }
                                } else {
                                    return Single.just(false);
                                }
                            }))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .subscribe(deleteReplaySubject);
            mHeadlessFragment.deleteBackupReplaySubjectMap.put(remoteBackupMetadata, deleteReplaySubject);
        }
        return deleteReplaySubject;
    }

    @NonNull
    synchronized Observable<Boolean> restoreBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData) {
        if (mHeadlessFragment.restoreBackupReplaySubjectMap == null) {
            mHeadlessFragment.restoreBackupReplaySubjectMap = new HashMap<>();
        }
        ReplaySubject<Boolean> restoreBackupReplaySubject = mHeadlessFragment.restoreBackupReplaySubjectMap.get(remoteBackupMetadata);
        if (restoreBackupReplaySubject == null) {
            restoreBackupReplaySubject = ReplaySubject.create();
            mBackupProvidersManager.restoreBackup(remoteBackupMetadata, overwriteExistingData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .subscribe(restoreBackupReplaySubject);
            mHeadlessFragment.restoreBackupReplaySubjectMap.put(remoteBackupMetadata, restoreBackupReplaySubject);
        }
        return restoreBackupReplaySubject;
    }

    @NonNull
    synchronized Observable<File> downloadBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean debugMode) {
        if (mHeadlessFragment.downloadBackupReplaySubjectMap == null) {
            mHeadlessFragment.downloadBackupReplaySubjectMap = new HashMap<>();
        }

        ReplaySubject<File> downloadBackupReplaySubjectMap = mHeadlessFragment.downloadBackupReplaySubjectMap.get(remoteBackupMetadata);
        if (downloadBackupReplaySubjectMap == null) {
            final File cacheDir = new SmartReceiptsTemporaryFileCache(mContext).getInternalCacheFile(FileUtils.omitIllegalCharactersFromFileName(remoteBackupMetadata.getSyncDeviceName()));
            final File cacheDirZipFile = new SmartReceiptsTemporaryFileCache(mContext).getInternalCacheFile(FileUtils.omitIllegalCharactersFromFileName(remoteBackupMetadata.getSyncDeviceName()) + ".zip");
            downloadBackupReplaySubjectMap = ReplaySubject.create();

            Completable.fromCallable(() -> {
                final StorageManager storageManager = StorageManager.getInstance(mContext);
                if (storageManager.deleteRecursively(cacheDir)) {
                    if ((cacheDir.exists() || cacheDir.mkdirs()) && (!cacheDirZipFile.exists() || cacheDirZipFile.delete())) {
                        return true;
                    } else {
                        throw new IOException("Failed to create cache directory to save the images");
                    }
                } else {
                    throw new IOException("Failed delete our previously cached directory");
                }
            })
                    .andThen(downloadData(remoteBackupMetadata, debugMode, cacheDir))
                    .map(files -> StorageManager.getInstance(mContext).zip(cacheDir))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .subscribe(downloadBackupReplaySubjectMap);

            mHeadlessFragment.downloadBackupReplaySubjectMap.put(remoteBackupMetadata, downloadBackupReplaySubjectMap);
        }
        return downloadBackupReplaySubjectMap;
    }

    @NonNull
    private Single<List<File>> downloadData(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean debugMode, File cacheDir) {
        if (debugMode) {
            return mBackupProvidersManager.debugDownloadAllData(remoteBackupMetadata, cacheDir);
        } else {
            return mBackupProvidersManager.downloadAllData(remoteBackupMetadata, cacheDir);
        }
    }

    synchronized void removeCachedRestoreBackupFor(@NonNull final RemoteBackupMetadata remoteBackupMetadata) {
        if (mHeadlessFragment.restoreBackupReplaySubjectMap != null) {
            mHeadlessFragment.restoreBackupReplaySubjectMap.remove(remoteBackupMetadata);
        }
        if (mHeadlessFragment.downloadBackupReplaySubjectMap != null) {
            mHeadlessFragment.downloadBackupReplaySubjectMap.remove(remoteBackupMetadata);
        }
    }

    @NonNull
    private Single<Optional<RemoteBackupMetadata>> getBackupMetadata(@Nullable RemoteBackupMetadata remoteBackupMetadata) {
        if (remoteBackupMetadata == null) {
            Logger.debug(this, "Attempting to fetch the local metadata");
            return mBackupProvidersManager.getRemoteBackups()
                    .map(remoteBackupMetadatas -> {
                        for (final RemoteBackupMetadata metadata : remoteBackupMetadatas) {
                            if (metadata.getSyncDeviceId().equals(mBackupProvidersManager.getDeviceSyncId())) {
                                Logger.info(this, "Identified the local device metadata as {}", metadata);
                                return Optional.of(metadata);
                            }
                        }
                        Logger.warn(this, "No local backup currently exists.");
                        return Optional.absent();
                    });
        } else {
            Logger.debug(this, "Returning the provided metadata: {}", remoteBackupMetadata);
            return Single.just(Optional.of(remoteBackupMetadata));
        }
    }

    @NonNull
    private Single<Boolean> deleteLocalSyncDataIfNeeded(@Nullable final RemoteBackupMetadata remoteBackupMetadata) {
        if (remoteBackupMetadata == null || remoteBackupMetadata.getSyncDeviceId().equals(mBackupProvidersManager.getDeviceSyncId())) {
            return mDatabaseHelper.getReceiptsTable().deleteSyncData(mBackupProvidersManager.getSyncProvider());
        } else {
            return Single.just(true);
        }
    }

    public static final class RemoteBackupsResultsCacheHeadlessFragment extends Fragment {

        private static final String TAG = RemoteBackupsDataCache.class.getName();

        private Map<SyncProvider, ReplaySubject<List<RemoteBackupMetadata>>> getBackupsReplaySubjectMap;
        private Map<RemoteBackupMetadata, ReplaySubject<Boolean>> deleteBackupReplaySubjectMap;
        private Map<RemoteBackupMetadata, ReplaySubject<Boolean>> restoreBackupReplaySubjectMap;
        private Map<RemoteBackupMetadata, ReplaySubject<File>> downloadBackupReplaySubjectMap;
        private Map<RemoteBackupMetadata, ReplaySubject<com.google.api.services.drive.model.File>> renameBackupReplaySubjectMap;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

}
