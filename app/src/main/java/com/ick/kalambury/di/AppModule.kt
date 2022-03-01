package com.ick.kalambury.di

import android.content.Context
import android.os.Vibrator
import androidx.annotation.Nullable
import androidx.core.content.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Nullable
    fun provideVibratorService(@ApplicationContext context: Context): Vibrator? =
        context.getSystemService()

}