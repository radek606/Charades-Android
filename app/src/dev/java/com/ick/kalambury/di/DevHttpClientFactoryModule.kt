package com.ick.kalambury.di

import com.ick.kalambury.net.DevHttpClientFactory
import com.ick.kalambury.net.HttpClientFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DevHttpClientFactoryModule {
    @Binds
    abstract fun bindHttpClientFactory(factory: DevHttpClientFactory): HttpClientFactory
}