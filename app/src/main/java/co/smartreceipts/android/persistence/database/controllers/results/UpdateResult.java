package co.smartreceipts.android.persistence.database.controllers.results;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;

public class UpdateResult<ModelType> {

    private final ModelType oldModel;
    private final ModelType newModel;
    private final Throwable throwable;
    private final DatabaseOperationMetadata databaseOperationMetadata;

    public UpdateResult(@NonNull ModelType oldModel, @NonNull ModelType newModel, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        this(oldModel, newModel, null, databaseOperationMetadata);
    }

    public UpdateResult(@NonNull ModelType oldModel, @NonNull Throwable throwable, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        this(oldModel, null, throwable, databaseOperationMetadata);
    }

    public UpdateResult(@NonNull ModelType oldModel, @Nullable ModelType newModel,
                        @Nullable Throwable throwable, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        this.oldModel = Preconditions.checkNotNull(oldModel);
        this.newModel = newModel;
        this.throwable = throwable;
        this.databaseOperationMetadata = Preconditions.checkNotNull(databaseOperationMetadata);
    }

    @NonNull
    public ModelType getOld() {
        return oldModel;
    }

    @Nullable
    public ModelType getNew() {
        return newModel;
    }

    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

    @NonNull
    public DatabaseOperationMetadata getDatabaseOperationMetadata() {
        return databaseOperationMetadata;
    }
}
