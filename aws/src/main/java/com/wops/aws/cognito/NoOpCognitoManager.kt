package com.wops.aws.cognito

import com.wops.core.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class NoOpCognitoManager @Inject constructor() : CognitoManager {
    override fun initialize() {}
}