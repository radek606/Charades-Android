package com.ick.kalambury.wordsrepository.migration

import com.ick.kalambury.wordsrepository.datasource.WordsManifestDataSource
import com.ick.kalambury.wordsrepository.datasource.WordsSetDataSource
import java.io.File

open class CompositeDataSource(
    val rootDirectory: () -> File,
    manifestDataSourceDelegate: WordsManifestDataSource,
    wordsSetDataSourceDelegate: WordsSetDataSource,
) : WordsManifestDataSource by manifestDataSourceDelegate,
    WordsSetDataSource by wordsSetDataSourceDelegate