package co.smartreceipts.android.identity.widget.di

import co.smartreceipts.android.identity.widget.account.AccountFragment
import co.smartreceipts.android.identity.widget.account.AccountView
import dagger.Binds
import dagger.Module

@Module
abstract class AccountModule {

    @Binds
    internal abstract fun provideAccountView(fragment: AccountFragment): AccountView

}
