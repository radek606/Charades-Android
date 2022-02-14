package com.ick.kalambury.di

import com.ick.kalambury.net.HttpClientFactory
import com.ick.kalambury.net.ProdHttpClientFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdHttpClientFactoryModule {
    @Binds
    abstract fun bindHttpClientFactory(factory: ProdHttpClientFactory): HttpClientFactory
}