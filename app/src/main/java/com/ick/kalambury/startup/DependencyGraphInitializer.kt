package com.ick.kalambury.startup

import android.content.Context
import androidx.startup.Initializer
import com.ick.kalambury.di.InitializerEntryPoint

class DependencyGraphInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        InitializerEntryPoint.resolve(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}