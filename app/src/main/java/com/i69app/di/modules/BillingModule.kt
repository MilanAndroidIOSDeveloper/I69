package com.i69app.di.modules

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import com.i69app.billing.BillingDataSource
import com.i69app.billing.BillingRepository
import com.i69app.data.config.Constants
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BillingModule {

    @Singleton
    @Provides
    fun provideBillingRepository(
        billingDataSource: BillingDataSource,
        defaultScope: CoroutineScope
    ) = BillingRepository(billingDataSource, defaultScope)

    @Singleton
    @Provides
    fun provideBillingDataSource(
        application: Application,
        defaultScope: CoroutineScope,
        knownInAppSkus: Array<String>
    ) = BillingDataSource(application, defaultScope, knownInAppSkus)

    @Singleton
    @Provides
    fun provideCoroutineScope(): CoroutineScope = GlobalScope

    @Singleton
    @Provides
    fun provideInAppSkus(): Array<String> = com.i69app.data.config.Constants.IN_APP_SKUS

}