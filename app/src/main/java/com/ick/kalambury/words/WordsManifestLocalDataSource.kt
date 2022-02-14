package com.ick.kalambury.words

import android.content.Context
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.util.JsonUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream


class WordsManifestLocalDataSource(
    private val context: Context,
) : WordsManifestDataSource {

    override fun getWordsManifest(): Single<WordsManifest> {
        return getLocalFileStream()
            .onErrorResumeNext { getAssetsFileStream() }
            .map { stream -> stream.use { JsonUtils.fromJson(it, WordsManifest::class.java) } }
    }

    private fun getAssetsFileStream(): Single<InputStream> {
        return Single.fromCallable {
            context.assets.open("${BuildConfig.LOCAL_WORDS_ROOT_DIR}/${BuildConfig.LOCAL_WORDS_MANIFEST_FILE_NAME}")
        }
    }

    private fun getLocalFileStream(): Single<InputStream> {
        return Single.fromCallable {
            FileInputStream(File(getWordsRootDirectory(), BuildConfig.LOCAL_WORDS_MANIFEST_FILE_NAME))
        }
    }

    override fun saveWordsManifest(wordsManifest: WordsManifest): Completable {
        return Completable.fromCallable {
            val outputFile = File(getWordsRootDirectory(), BuildConfig.LOCAL_WORDS_MANIFEST_FILE_NAME)
            FileOutputStream(outputFile).use {
                JsonUtils.toJson(it, wordsManifest)
            }
        }
    }

    private fun getWordsRootDirectory(): File {
        val directory = File(context.filesDir, BuildConfig.LOCAL_WORDS_ROOT_DIR)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

}