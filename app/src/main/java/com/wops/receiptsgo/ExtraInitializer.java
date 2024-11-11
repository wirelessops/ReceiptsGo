package com.wops.receiptsgo;

/**
 * For additional version-dependent initialization
 */
public interface ExtraInitializer {

    /**
     * Initializes any extra items that may be required for this flavor to function properly
     */
    void init();

}
