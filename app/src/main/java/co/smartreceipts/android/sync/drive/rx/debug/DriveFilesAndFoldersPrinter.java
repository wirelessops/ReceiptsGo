package co.smartreceipts.android.sync.drive.rx.debug;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import java.io.IOException;
import java.util.Date;

import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

/**
 * A debug utility, which will log all drive files and folders
 */
public class DriveFilesAndFoldersPrinter {

    public static void logAllFilesAndFolders(@NonNull GoogleApiClient googleApiClient) {
        Logger.error(DriveFilesAndFoldersPrinter.class, "***** Starting Drive Printing Routine *****");
        Completable.create(emitter -> {
            final SortOrder sortOrder = new SortOrder.Builder().addSortAscending(SortableField.MODIFIED_DATE).build();
            final Query query = new Query.Builder().setSortOrder(sortOrder).build();
            Drive.DriveApi.query(googleApiClient, query).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {

                @Override
                public void onSuccess(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                    try {
                        for (final Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                            final String title = metadata.getTitle();
                            final String fileName = metadata.getOriginalFilename();
                            final long size = metadata.getFileSize();
                            final Date createdAt = metadata.getCreatedDate();
                            final Date modifiedDate = metadata.getModifiedDate();
                            final boolean isFolder = metadata.isFolder();
                            final boolean inAppFolder = metadata.isInAppFolder();
                            final String id = metadata.getDriveId().getResourceId();
                            Logger.info(DriveFilesAndFoldersPrinter.class, "Found drive file:\n" +
                                    "{\n" +
                                    "  \"title\": \"{}\",\n" +
                                    "  \"fileName\": \"{}\",\n" +
                                    "  \"size\": \"{}\",\n" +
                                    "  \"createdAt\": \"{}\",\n" +
                                    "  \"modifiedDate\": \"{}\",\n" +
                                    "  \"isFolder\": \"{}\",\n" +
                                    "  \"inAppFolder\": \"{}\",\n" +
                                    "  \"id\": \"{}\"\n" +
                                    "},",
                                    title, fileName, size, createdAt, modifiedDate, isFolder, inAppFolder, id
                                    );
                        }
                    }
                    finally {
                        metadataBufferResult.getMetadataBuffer().release();
                        emitter.onComplete();
                    }
                }

                @Override
                public void onFailure(@NonNull Status status) {
                    Logger.error(DriveFilesAndFoldersPrinter.class, "Failed to query with status: " + status);
                    emitter.onError(new IOException(status.getStatusMessage()));
                }
            });
        })
        .subscribeOn(Schedulers.io())
        .subscribe(() -> {}, e -> {
            Logger.error(DriveFilesAndFoldersPrinter.class, "Failed to print", e);
        });
    }
}
