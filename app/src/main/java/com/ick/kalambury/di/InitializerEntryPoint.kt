package com.ick.kalambury.di

import android.content.Context
import com.ick.kalambury.startup.CrashHandlingInitializer
import com.ick.kalambury.startup.LogInitializer
import com.ick.kalambury.startup.WordsRepositoryInitializer
import com.ick.kalambury.startup.WorkManagerInitializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InitializerEntryPoint {

    fun inject(initializer: LogInitializer)
    fun inject(initializer: CrashHandlingInitializer)
    fun inject(initializer: WordsRepositoryInitializer)
    fun inject(initializer: WorkManagerInitializer)

    companion object {

        fun resolve(context: Context): InitializerEntryPoint {
            return EntryPointAccessors.fromApplication(
                context.applicationContext,
                InitializerEntryPoint::class.java
            )
        }

    }

}