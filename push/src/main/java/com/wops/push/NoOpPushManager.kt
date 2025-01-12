package com.wops.push

import com.wops.core.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class NoOpPushManager @Inject constructor(): PushManager {
    override fun initialize() {}
}