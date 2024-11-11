package com.wops.receiptsgo.di

import com.wops.receiptsgo.identity.IdentityManagerImpl
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.core.identity.IdentityManager
import dagger.Binds
import dagger.Module

@Module
abstract class IdentityModule {

    @Binds
    @ApplicationScope
    abstract fun provideIdentityManager(identityManager: IdentityManagerImpl) : IdentityManager
}