package com.ick.kalambury.wordsrepository.model

import com.ick.kalambury.wordsrepository.Language
import com.ick.kalambury.wordsrepository.Usage
import java.util.*

object TestData {

    const val set1Id = "1"
    const val set1Name = "set1"
    val set1word1 = Word(listOf("word11"), set1Name)
    val set1word2 = Word(listOf("word12"), set1Name)
    val set1word3 = Word(listOf("word13"), set1Name)
    val set1word4 = Word(listOf("word14"), set1Name)
    val set1word5 = Word(listOf("word15"), set1Name)
    val set1: WordsSet
        get() = WordsSet(set1Id, set1Name, mutableListOf(set1word1, set1word2, set1word3, set1word4, set1word5))

    const val set2Id = "2"
    const val set2Name = "set2"
    val set2word1 = Word(listOf("word21"), set2Name)
    val set2word2 = Word(listOf("word22"), set2Name)
    val set2word3 = Word(listOf("word23"), set2Name)
    val set2word4 = Word(listOf("word24"), set2Name)
    val set2word5 = Word(listOf("word25"), set2Name)
    val set2: WordsSet
        get() = WordsSet(set2Id, set2Name, mutableListOf(set2word1, set2word2, set2word3, set2word4, set2word5))

    const val set3Id = "3"
    const val set3Name = "set3"
    val set3word1 = Word(listOf("word31"), set3Name)
    val set3word2 = Word(listOf("word32"), set3Name)
    val set3word3 = Word(listOf("word33"), set3Name)
    val set3word4 = Word(listOf("word34"), set3Name)
    val set3word5 = Word(listOf("word35"), set3Name)
    val set3: WordsSet
        get() = WordsSet(set3Id, set3Name, mutableListOf(set3word1, set3word2, set3word3, set3word4, set3word5))

    const val set4Id = "4"
    const val set4Name = "set4"
    val set4word1 = Word(listOf("word41"), set4Name)
    val set4word2 = Word(listOf("word42"), set4Name)
    val set4word3 = Word(listOf("word43"), set4Name)
    val set4word4 = Word(listOf("word44"), set4Name)
    val set4word5 = Word(listOf("word45"), set4Name)
    val set4: WordsSet
        get() = WordsSet(set4Id, set4Name, mutableListOf(set4word1, set4word2, set4word3, set4word4, set4word5))

    val wordsSetsMap
        get() = mapOf(set1Id to set1, set2Id to set2, set3Id to set3, set4Id to set4)

    val showingOnlySet = WordsManifest.Set(
        set1Id, 0, set1Name, "showingOnlySet", Language.PL, Date(0), Date(0),
        mutableMapOf(Usage.SHOWING to WordsManifest.Set.Options(true))
    )

    val drawingOnlySet = WordsManifest.Set(
        set2Id, 0, set2Name, "drawingOnlySet", Language.PL, Date(0), Date(0),
        mutableMapOf(
            Usage.DRAWING to WordsManifest.Set.Options(false),
        )
    )

    val allOptionsDefaultSet = WordsManifest.Set(
        set3Id, 0, set3Name, "allOptionsDefaultSet", Language.PL, Date(0), Date(0),
        mutableMapOf(
            Usage.SHOWING to WordsManifest.Set.Options(true),
            Usage.DRAWING to WordsManifest.Set.Options(true)
        )
    )

    val allOptionsNotDefaultSet = WordsManifest.Set(
        set3Id, 0, set3Name, "allOptionsNotDefaultSet", Language.PL, Date(0), Date(0),
        mutableMapOf(
            Usage.SHOWING to WordsManifest.Set.Options(false),
            Usage.DRAWING to WordsManifest.Set.Options(false)
        )
    )

    val newlyAddedRemoteSet = WordsManifest.Set(
        set1Id, 1, set1Name, "newlyAddedRemoteSet", Language.PL, Date(System.currentTimeMillis()), Date(System.currentTimeMillis()),
        mutableMapOf(Usage.SHOWING to WordsManifest.Set.Options(true))
    )

    val newlyUpdatedRemoteSet = WordsManifest.Set(
        set1Id, 2, set1Name, "newlyUpdatedRemoteSet", Language.PL, Date(0), Date(System.currentTimeMillis()),
        mutableMapOf(Usage.SHOWING to WordsManifest.Set.Options(true))
    )

    val earlierUpdatedRemoteSet = WordsManifest.Set(
        set1Id, 3, set1Name, "earlierUpdatedRemoteSet", Language.PL, Date(0),
        Date(System.currentTimeMillis() - WordsManifest.Set.MARK_AS_NEW_OR_UPDATED_PERIOD),
        mutableMapOf(Usage.SHOWING to WordsManifest.Set.Options(true))
    )

    val manifest = WordsManifest(sets = listOf(showingOnlySet, drawingOnlySet, allOptionsDefaultSet,
        allOptionsNotDefaultSet, newlyAddedRemoteSet, newlyUpdatedRemoteSet, earlierUpdatedRemoteSet))

}