package com.ick.kalambury.di

import com.ick.kalambury.util.MainSchedulerProvider
import com.ick.kalambury.util.SchedulerProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class SchedulersModule {

    @Provides
    fun provideSchedulerProvider(): SchedulerProvider {
        return MainSchedulerProvider
    }

}