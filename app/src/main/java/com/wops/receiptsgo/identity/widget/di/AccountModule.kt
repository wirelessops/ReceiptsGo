package com.wops.receiptsgo.identity.widget.di

import com.wops.receiptsgo.identity.widget.account.AccountFragment
import com.wops.receiptsgo.identity.widget.account.AccountView
import dagger.Binds
import dagger.Module

@Module
abstract class AccountModule {

    @Binds
    internal abstract fun provideAccountView(fragment: AccountFragment): AccountView

}
