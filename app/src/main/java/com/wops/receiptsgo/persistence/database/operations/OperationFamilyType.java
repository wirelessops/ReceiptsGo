package com.wops.receiptsgo.persistence.database.operations;

public enum OperationFamilyType {
    /**
     * The default type of operation
     */
    Default,

    /**
     * Used specifically when importing files from a backup
     */
    Import,

    /**
     * Used to identify a sync operation
     */
    Sync,

    /**
     * Used to track when we're rolling back a failed insert/etc
     */
    Rollback,

    /**
     * Used to indicate that our listeners should not be informed. Note: Errors ignore this flag (no reason that they couldn't also respect it for right now though)
     */
    Silent
}
