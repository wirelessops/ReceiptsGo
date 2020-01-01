package co.smartreceipts.android.di

import co.smartreceipts.android.identity.IdentityManager
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.core.identity.IdentityManagerInterface
import dagger.Module
import dagger.Provides

@Module
class IdentityModule {

    @Provides
    @ApplicationScope
    fun provideIdentityManager(identityManager: IdentityManager) : IdentityManagerInterface {
        return identityManager
    }
}