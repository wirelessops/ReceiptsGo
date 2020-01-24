package co.smartreceipts.push

import co.smartreceipts.core.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class NoOpPushManager @Inject constructor(): PushManager {
    override fun initialize() {}
}