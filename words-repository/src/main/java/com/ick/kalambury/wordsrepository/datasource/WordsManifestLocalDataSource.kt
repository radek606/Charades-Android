package com.ick.kalambury.wordsrepository.datasource

import android.content.Context
import com.ick.kalambury.util.JsonUtils
import com.ick.kalambury.wordsrepository.BuildConfig
import com.ick.kalambury.wordsrepository.model.WordsManifest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class WordsManifestLocalDataSource(
    private val context: Context,
    private val rootDirectory: () -> File,
) : WordsManifestDataSource {

    override fun getAssetsWordsManifest(): Single<WordsManifest> {
        return Single.fromCallable { context.assets.open("words/manifest.json") }
            .map { stream -> stream.use { JsonUtils.fromJson(it, WordsManifest::class.java) } }
    }

    override fun getLocalWordsManifest(): Single<WordsManifest> {
        return Single.fromCallable { FileInputStream(File(rootDirectory.invoke(), BuildConfig.WORDS_MANIFEST_FILE_NAME)) }
            .map { stream -> stream.use { JsonUtils.fromJson(it, WordsManifest::class.java) } }
    }

    override fun saveWordsManifest(wordsManifest: WordsManifest): Completable {
        return Completable.fromCallable {
            val outputFile = File(rootDirectory.invoke(), BuildConfig.WORDS_MANIFEST_FILE_NAME)
            FileOutputStream(outputFile).use { JsonUtils.toJson(it, wordsManifest) }
        }
    }

}