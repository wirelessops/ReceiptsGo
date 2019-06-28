package co.smartreceipts.android.persistence.database.controllers.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;

public class StubTableEventsListener<T> implements TableEventsListener<T> {

    @Override
    public void onGetSuccess(@NonNull List<T> list) {

    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {

    }

    @Override
    public void onInsertSuccess(@NonNull T t, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onInsertFailure(@NonNull T t, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onUpdateSuccess(@NonNull T oldT, @NonNull T newT, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onUpdateFailure(@NonNull T oldT, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onDeleteSuccess(@NonNull T t, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onDeleteFailure(@NonNull T t, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }
}
