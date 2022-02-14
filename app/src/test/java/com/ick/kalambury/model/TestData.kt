package com.ick.kalambury.model

import com.ick.kalambury.GameMode
import com.ick.kalambury.words.*
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

    val wordsSetsMap
        get() = mapOf(set1Id to set1, set2Id to set2, set3Id to set3)

    val set1Info = WordsManifest.Set(set1Id, 0, set1Name, "set1Desc", "pl", Date(0), Date(0),
        mutableMapOf(GameMode.SHOWING.wordsSetType to WordsManifest.Set.Options(true)))

    val set2Info = WordsManifest.Set(set2Id, 0, set2Name, "set2Desc", "pl", Date(0), Date(0),
        mutableMapOf(GameMode.SHOWING.wordsSetType to WordsManifest.Set.Options(true),
            GameMode.DRAWING_LOCAL.wordsSetType to WordsManifest.Set.Options(false),
            GameMode.DRAWING_ONLINE.wordsSetType to WordsManifest.Set.Options(false)))
    val set3Info = WordsManifest.Set(set1Id, 0, set1Name, "set1Desc", "pl", Date(0), Date(0),
        mutableMapOf(GameMode.SHOWING.wordsSetType to WordsManifest.Set.Options(true),
            GameMode.DRAWING_LOCAL.wordsSetType to WordsManifest.Set.Options(true),
            GameMode.DRAWING_ONLINE.wordsSetType to WordsManifest.Set.Options(true)))

    val manifest = WordsManifest(version = 0, sets = listOf(set1Info, set2Info, set3Info))

}