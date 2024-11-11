package com.wops.receiptsgo.persistence.database.controllers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;

public interface TableEventsListener<T> {

    void onGetSuccess(@NonNull List<T> list);

    void onGetFailure(@Nullable Throwable e);

    void onInsertSuccess(@NonNull T t, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    void onInsertFailure(@NonNull T t, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    void onUpdateSuccess(@NonNull T oldT, @NonNull T newT, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    void onUpdateFailure(@NonNull T oldT, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    void onDeleteSuccess(@NonNull T t, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

    void onDeleteFailure(@NonNull T t, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata);

}
