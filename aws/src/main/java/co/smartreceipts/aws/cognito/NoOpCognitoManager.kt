package co.smartreceipts.aws.cognito

import co.smartreceipts.core.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class NoOpCognitoManager @Inject constructor() : CognitoManager {
    override fun initialize() {}
}