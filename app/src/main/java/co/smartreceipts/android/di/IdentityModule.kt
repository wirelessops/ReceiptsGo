package co.smartreceipts.android.di

import co.smartreceipts.android.identity.IdentityManagerImpl
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