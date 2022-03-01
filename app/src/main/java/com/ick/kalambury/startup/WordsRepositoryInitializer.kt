package com.ick.kalambury.startup

import android.content.Context
import androidx.startup.Initializer
import com.ick.kalambury.di.InitializerEntryPoint
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.wordsrepository.WordsRepository
import javax.inject.Inject

class WordsRepositoryInitializer: Initializer<Unit> {

    @Inject
    lateinit var wordsRepository: WordsRepository

    override fun create(context: Context) {
        //do nothing except injecting WordsRepository, to force eager initialization
        Log.d(logTag(), "Initializing words repository...")

        InitializerEntryPoint.resolve(context).inject(this)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(
            DependencyGraphInitializer::class.java,
            LogInitializer::class.java
        )
    }

}