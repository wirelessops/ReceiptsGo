/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by Steven Hanus for Smart Receipts - 08/08/19
 */
package co.smartreceipts.android.sync.drive;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Preconditions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {

  private static final int BYTE_BUFFER_SIZE = 8192;

  //TODO: update this when we are moving to public folder
  private static final String DRIVE_SEARCH_AREA = "appDataFolder";

  private final Context context;
  private final Drive driveService;

  DriveServiceHelper(@NonNull Context context, @NonNull Drive driveService) {
    this.context = Preconditions.checkNotNull(context);
    this.driveService = Preconditions.checkNotNull(driveService);
  }

  /**
   * Creates a file in the user's application folder and returns it.
   */
  //TODO: Determine if we need the entire file returned, if not, we can call setFields()
  //      on the execution to tell the api what data of the created file to return
  public Single<File> createFile(@Nullable String name, @Nullable String mimeType, @Nullable String description,
                                 @Nullable Map<String, String> properties, @Nullable String folderDestination,
                                 @Nullable java.io.File javaFile) {
    return Single.fromCallable(() -> {
      File metadata = new File();

      if (!TextUtils.isEmpty(folderDestination)) {
        metadata.setParents(Collections.singletonList(folderDestination));
      }

      if (!TextUtils.isEmpty(mimeType)) {
        metadata.setMimeType(mimeType);
      }

      if (!TextUtils.isEmpty(name)) {
        metadata.setName(name);
      }

      if (!TextUtils.isEmpty(description)) {
        metadata.setDescription(description);
      }

      if (properties != null) {
        metadata.setProperties(properties);
      }

      File googleFile;
      if (javaFile != null) {
        if (!TextUtils.isEmpty(mimeType)) {
          FileContent mediaContent = new FileContent(mimeType, javaFile);
          googleFile = driveService.files().create(metadata, mediaContent).execute();
        } else {
          final Uri uri = Uri.fromFile(javaFile);
          final String mime = UriUtils.getMimeType(uri, context.getContentResolver());
          metadata.setMimeType(mime);
          FileContent mediaContent = new FileContent(mime, javaFile);
          googleFile = driveService.files().create(metadata, mediaContent).execute();
        }
      } else {
        googleFile = driveService.files().create(metadata).execute();
      }

      if (googleFile == null) {
        throw new IOException("Null result when requesting file creation.");
      }

      return googleFile;
    });
  }

  /**
   * Returns a {@link FileList} containing all the visible files in the app folder.
   *
   * <p>The returned list will only contain files visible to this app, i.e. those which were
   * created by this app. To perform operations on files not created by the app, the project must
   * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
   * Developer's Console</a> and be submitted to Google for verification.</p>
   *
   */
  public Single<FileList> querySingle(String query) {
    return Single.fromCallable(() ->
            driveService.files().list().setQ(query).setSpaces(DRIVE_SEARCH_AREA).setFields("*").execute());
  }

  public Observable<FileList> queryObservable(String query) {
    return Observable.fromCallable(() ->
            driveService.files().list().setQ(query).setSpaces(DRIVE_SEARCH_AREA).setFields("*").execute());
  }

  public Completable deleteFile(String fileId) {
    return Completable.fromCallable(() -> driveService.files().delete(fileId).execute());
  }

  public Single<FileList> getAllFilesSortedByTime() {
    return Single.fromCallable(() ->
            driveService.files().list().setSpaces(DRIVE_SEARCH_AREA).setOrderBy("modifiedTime").setFields("*").execute());
  }

  public Single<java.io.File> getDriveFileAsJavaFile(String fileId, java.io.File downloadLocationFile) {
    return Single.fromCallable(() -> driveService.files().get(fileId).setFields("*").executeMediaAsInputStream())
            .flatMap(inputStream -> {
              FileOutputStream fileOutputStream = null;
              try {
                fileOutputStream = new FileOutputStream(downloadLocationFile);
                byte[] buffer = new byte[BYTE_BUFFER_SIZE];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                  fileOutputStream.write(buffer, 0, read);
                }
                return Single.just(downloadLocationFile);

              } catch (IOException e) {
                Logger.error(DriveServiceHelper.this, "Failed write file with exception: ", e);
                return Single.error(e);
              } finally {
                StorageManager.closeQuietly(inputStream);
                StorageManager.closeQuietly(fileOutputStream);
              }
            });
  }

  public Single<File> getFile(String fileId) {
    return Single.fromCallable(() -> driveService.files().get(fileId).setFields("*").execute());
  }

  public Single<FileList> getFilesInFolder(String folderId) {
    String query = "'".concat(folderId).concat("' in parents");
    return Single.fromCallable(() ->
            driveService.files().list().setQ(query).setSpaces(DRIVE_SEARCH_AREA).setFields("*").execute());
  }

  public Single<FileList> getFilesByNameInFolder(String folderId, String fileName) {
    String query = "'".concat(folderId).concat("' in parents and name = '".concat(fileName).concat("'"));
    return Single.fromCallable(() ->
            driveService.files().list().setQ(query).setSpaces(DRIVE_SEARCH_AREA).setFields("*").execute());
  }

  public Single<File> updateFile(String fileId, java.io.File file) {

    final Uri uri = Uri.fromFile(file);
    final String mimeType = UriUtils.getMimeType(uri, context.getContentResolver());

    return querySingle("name = '".concat(file.getName()).concat("'"))
            .flatMap(fileList -> {
              if (fileList.getFiles().isEmpty()) {
                return createFile(file.getName(), mimeType, null,
                        null, DRIVE_SEARCH_AREA, file);
              } else {
                  File driveFile = new File();
                  // File's new content.
                  FileContent mediaContent = new FileContent(mimeType, file);
                  driveFile.setMimeType(mimeType);
                  driveFile.setName(file.getName());
                  return Single.fromCallable(() ->
                          driveService.files().update(fileId, driveFile, mediaContent).execute());
              }
            });
  }

}