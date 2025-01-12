package com.wops.receiptsgo.sync.network;

interface PossibleNetworkChangeListener {

    void initialize();

    void deinitialize();

    void onPossibleNetworkStateChange();
}
