package co.smartreceipts.android.persistence.database.operations;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class DatabaseOperationMetadata {

    private final OperationFamilyType operationFamilyType;

    public DatabaseOperationMetadata() {
        this(OperationFamilyType.Default);
    }

    public DatabaseOperationMetadata(@NonNull OperationFamilyType operationFamilyType) {
        this.operationFamilyType = Preconditions.checkNotNull(operationFamilyType);
    }

    @NonNull
    public OperationFamilyType getOperationFamilyType() {
        return operationFamilyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseOperationMetadata)) return false;

        DatabaseOperationMetadata that = (DatabaseOperationMetadata) o;

        return operationFamilyType == that.operationFamilyType;

    }

    @Override
    public int hashCode() {
        return operationFamilyType.hashCode();
    }


    @Override
    public String toString() {
        return "DatabaseOperationMetadata{" + operationFamilyType.name() +"}";
    }
}
