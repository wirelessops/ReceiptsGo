package co.smartreceipts.android.sync.drive.rx.debug;

import androidx.annotation.NonNull;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * A debug utility, which will log all drive files and folders
 */
public class DriveFilesAndFoldersPrinter {

    @SuppressWarnings("unused")
    public static void logAllFilesAndFolders(@NonNull Drive driveService) {
        Logger.error(DriveFilesAndFoldersPrinter.class, "***** Starting Drive Printing Routine *****");
        Single.fromCallable(() -> driveService.files().list().setSpaces("appDataFolder").setOrderBy("modifiedTime").setFields("*").execute())
                .doOnSuccess(fileList -> {
                    for (File f : fileList.getFiles()) {
                        final String title = f.getName();
                        final String fileName = f.getOriginalFilename();
                        final long size = f.getSize();
                        final DateTime createdAt = f.getCreatedTime();
                        final DateTime modifiedDate = f.getModifiedTime();
                        final boolean isFolder = f.getMimeType().equals("application/vnd.google-apps.folder");
                        final String id = f.getId();

                        Logger.info(DriveFilesAndFoldersPrinter.class, "Found drive file:\n" +
                                    "{\n" +
                                    "  \"title\": \"{}\",\n" +
                                    "  \"fileName\": \"{}\",\n" +
                                    "  \"size\": \"{}\",\n" +
                                    "  \"createdAt\": \"{}\",\n" +
                                    "  \"modifiedDate\": \"{}\",\n" +
                                    "  \"isFolder\": \"{}\",\n" +
                                    "  \"id\": \"{}\"\n" +
                                    "},",
                                    title, fileName, size, createdAt, modifiedDate, isFolder, id
                                    );
                    }
                })
                .doOnError(throwable ->
                    Logger.error(DriveFilesAndFoldersPrinter.class, "Failed to query with status: " + throwable.getMessage()))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
