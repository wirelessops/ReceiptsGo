package co.smartreceipts.android.sync.drive.rx;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.error.DriveThrowableToSyncErrorTranslator;
import co.smartreceipts.android.sync.drive.services.DriveUploadCompleteManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.Subject;


public class DriveStreamsManager implements GoogleApiClient.ConnectionCallbacks {

    private final DriveDataStreams driveDataStreams;
    private final DriveStreamMappings driveStreamMappings;
    private final Subject<Optional<Throwable>> driveErrorStream;
    private final DriveThrowableToSyncErrorTranslator syncErrorTranslator;
    private final AtomicReference<CountDownLatch> latchReference;

    public DriveStreamsManager(@NonNull Context context, @NonNull GoogleApiClient googleApiClient, @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                               @NonNull Subject<Optional<Throwable>> driveErrorStream, @NonNull DriveUploadCompleteManager driveUploadCompleteManager) {
        this(new DriveDataStreams(context, googleApiClient, googleDriveSyncMetadata, driveUploadCompleteManager), new DriveStreamMappings(), driveErrorStream, new DriveThrowableToSyncErrorTranslator());
    }

    @VisibleForTesting
    DriveStreamsManager(@NonNull DriveDataStreams driveDataStreams, @NonNull DriveStreamMappings driveStreamMappings,
                               @NonNull Subject<Optional<Throwable>> driveErrorStream,
                               @NonNull DriveThrowableToSyncErrorTranslator syncErrorTranslator) {
        this.driveDataStreams = Preconditions.checkNotNull(driveDataStreams);
        this.driveStreamMappings = Preconditions.checkNotNull(driveStreamMappings);
        this.driveErrorStream = Preconditions.checkNotNull(driveErrorStream);
        this.syncErrorTranslator = Preconditions.checkNotNull(syncErrorTranslator);
        this.latchReference = new AtomicReference<>(new CountDownLatch(1));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Logger.info(this, "GoogleApiClient connection succeeded.");
        latchReference.get().countDown();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Logger.info(this, "GoogleApiClient connection suspended with cause {}", cause);
        latchReference.set(new CountDownLatch(1));
    }

    @NonNull
    public Single<List<RemoteBackupMetadata>> getRemoteBackups() {
        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.getSmartReceiptsFolders())
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public synchronized Single<DriveId> getDriveId(@NonNull final Identifier identifier) {
        Preconditions.checkNotNull(identifier);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.getDriveId(identifier))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public synchronized Observable<DriveId> getFilesInFolder(@NonNull final DriveFolder driveFolder) {
        Preconditions.checkNotNull(driveFolder);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.getFilesInFolder(driveFolder))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public synchronized Observable<DriveId> getFilesInFolder(@NonNull final DriveFolder driveFolder, @NonNull final String fileName) {
        Preconditions.checkNotNull(driveFolder);
        Preconditions.checkNotNull(fileName);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.getFilesInFolder(driveFolder, fileName))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public synchronized Single<Metadata> getMetadata(@NonNull final DriveFile driveFile) {
        Preconditions.checkNotNull(driveFile);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.getMetadata(driveFile))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public synchronized Single<List<Metadata>> getParents(@NonNull final DriveFile driveFile) {
        Preconditions.checkNotNull(driveFile);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.getParents(driveFile))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public Single<SyncState> uploadFileToDrive(@NonNull final SyncState currentSyncState, @NonNull final File file) {
        Preconditions.checkNotNull(currentSyncState);
        Preconditions.checkNotNull(file);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.getSmartReceiptsFolder())
                .firstOrError() // hack. because getSmartReceiptsFolder emits just once
                .flatMap(driveFolder -> driveDataStreams.createFileInFolder(driveFolder, file))
                .flatMap(driveFile -> Single.just(driveStreamMappings.postInsertSyncState(currentSyncState, driveFile)))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public Single<Identifier> uploadFileToDrive(@NonNull final File file) {
        Preconditions.checkNotNull(file);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.getSmartReceiptsFolder())
                .firstOrError() // hack. because getSmartReceiptsFolder emits just once
                .flatMap(driveFolder -> driveDataStreams.createFileInFolder(driveFolder, file))
                .flatMap(driveFile -> Single.just(new Identifier(driveFile.getDriveId().getResourceId())))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public Single<SyncState> updateDriveFile(@NonNull final SyncState currentSyncState, @NonNull final File file) {
        Preconditions.checkNotNull(currentSyncState);
        Preconditions.checkNotNull(file);

        return newBlockUntilConnectedCompletable()
                .andThen(updateDrive(currentSyncState, file))
                .flatMap(driveFile -> Single.just(driveStreamMappings.postUpdateSyncState(currentSyncState, driveFile)))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public Single<Identifier> updateDriveFile(@NonNull final Identifier currentIdentifier, @NonNull final File file) {
        Preconditions.checkNotNull(currentIdentifier);
        Preconditions.checkNotNull(file);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.updateFile(currentIdentifier, file))
                .flatMap(driveFile -> Single.just(new Identifier(driveFile.getDriveId().getResourceId())))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    public Single<SyncState> deleteDriveFile(@NonNull final SyncState currentSyncState, final boolean isFullDelete) {
        Preconditions.checkNotNull(currentSyncState);

        return newBlockUntilConnectedCompletable()
                .andThen(deleteDrive(currentSyncState))
                .flatMap(success -> {
                    if(success) {
                        return Single.just(driveStreamMappings.postDeleteSyncState(currentSyncState, isFullDelete));
                    } else {
                        return Single.just(currentSyncState);
                    }
                })
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    private Single<Boolean> deleteDrive(@NonNull SyncState currentSyncState) {
        final Identifier driveIdentifier = currentSyncState.getSyncId(SyncProvider.GoogleDrive);
        if (driveIdentifier != null) {
            return driveDataStreams.delete(driveIdentifier);
        } else {
            return Single.just(true);
        }
    }

    private Single<DriveFile> updateDrive(@NonNull final SyncState currentSyncState, @NonNull final File file) {
        final Identifier driveIdentifier = currentSyncState.getSyncId(SyncProvider.GoogleDrive);
        if (driveIdentifier != null) {
            return driveDataStreams.updateFile(driveIdentifier, file);
        } else {
            return Single.error(new Exception("This sync state doesn't include a valid Drive Identifier"));
        }
    }

    @NonNull
    public Single<Boolean> delete(@NonNull final Identifier identifier) {
        Preconditions.checkNotNull(identifier);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.delete(identifier))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    public void clearCachedData() {
        driveDataStreams.clear();
    }

    @NonNull
    public Single<File> download(@NonNull final DriveFile driveFile, @NonNull final File downloadLocationFile) {
        Preconditions.checkNotNull(driveFile);
        Preconditions.checkNotNull(downloadLocationFile);

        return newBlockUntilConnectedCompletable()
                .andThen(driveDataStreams.download(driveFile, downloadLocationFile))
                .doOnError(throwable -> driveErrorStream.onNext(Optional.of(syncErrorTranslator.get(throwable))));
    }

    @NonNull
    private Completable newBlockUntilConnectedCompletable() {
        return Completable.fromAction(() -> {
            final CountDownLatch countDownLatch = latchReference.get();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new Exception("newBlockUntilConnectedCompletable failed");
            }
        });
    }

}
