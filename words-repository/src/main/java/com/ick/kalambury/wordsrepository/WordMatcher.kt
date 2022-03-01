package com.ick.kalambury.wordsrepository

import com.ick.kalambury.wordsrepository.model.Word
import com.ick.kalambury.wordsrepository.model.WordMatchingResult
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object WordMatcher {

    private const val WORDS_PAIR_LENGTH_DIFF_FACTOR = 0.3f
    private const val CLOSE_ENOUGH_ANSWER_DISTANCE_FACTOR = 0.3f

    private val charsMapping: MutableMap<Char, Char> = Hashtable()
    init {
        charsMapping['ą'] = 'a'
        charsMapping['ć'] = 'c'
        charsMapping['ę'] = 'e'
        charsMapping['ł'] = 'l'
        charsMapping['ń'] = 'n'
        charsMapping['ó'] = 'o'
        charsMapping['ś'] = 's'
        charsMapping['ź'] = 'z'
        charsMapping['ż'] = 'z'
    }

    fun matchAnswer(word: Word?, answer: String?): WordMatchingResult {
        if (word == null || answer == null) {
            return WordMatchingResult("", "", isMatch = false, isCloseEnough = false)
        }

        val matchedVariant = getExactMatch(word, answer)
        if (matchedVariant != null) {
            return WordMatchingResult(matchedVariant, answer, isMatch = true, isCloseEnough = true)
        }

        val splitAnswer = answer.split("\\s".toRegex())
        for (variant in word.variants) {
            val splitVariant = variant.split("\\s".toRegex())

            if (splitAnswer.size > splitVariant.size + 1) break

            for (variantPart in splitVariant) {
                if (!isValidWord(variantPart)) continue

                val maxDistance = max(1, (variantPart.length * CLOSE_ENOUGH_ANSWER_DISTANCE_FACTOR).roundToInt())

                for (answerPart in splitAnswer) {
                    if (!isValidPair(variantPart, answerPart)) continue

                    val normalizedAnswerPart = normalizeString(answerPart)
                    val distance = levenshteinDistance(variantPart, normalizedAnswerPart)
                    if (distance <= maxDistance) {
                        return WordMatchingResult(variant, answer, isMatch = false, isCloseEnough = true)
                    }
                }
            }
        }
        return WordMatchingResult(word.wordString, answer, isMatch = false, isCloseEnough = false)
    }

    private fun getExactMatch(word: Word, answer: String): String? {
        val normalizedAnswer = normalizeString(answer)
        for (variant in word.variants) {
            val normalizedVariant = normalizeString(variant)
            if (normalizedVariant == normalizedAnswer) {
                return variant
            }
        }
        return null
    }

    private fun isValidPair(variantWord: String, answerWord: String): Boolean {
        val variantLength = variantWord.length
        val answerLength = answerWord.length

        //ignore all words with length <= 2, e.g. some conjunctions or prepositions
        if (variantLength <= 2 || answerLength <= 2) {
            return false
        }

        val lengthDiff = abs(variantLength - answerLength)

        //for 3-letter words length diff must be 0 (they are to short to allow differences)
        if (variantLength == 3 && lengthDiff != 0) {
            return false
        }

        val maxLengthDiff = max(1, (variantWord.length * WORDS_PAIR_LENGTH_DIFF_FACTOR).roundToInt())
        
        return lengthDiff <= maxLengthDiff
    }

    private fun isValidWord(word: String): Boolean {
        return word.length > 2
    }

    private fun normalizeString(string: String): String {
        return replaceChars(string.lowercase(Locale.getDefault()).replace("\\s".toRegex(), ""))
    }

    private fun replaceChars(word: String): String {
        var replaced = word
        for ((key, value) in charsMapping) {
            replaced = replaced.replace(key, value)
        }
        return replaced
    }

    //https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
    private fun levenshteinDistance(lhs: String, rhs: String): Int {
        val len0 = lhs.length + 1
        val len1 = rhs.length + 1
        var cost = IntArray(len0)
        var newcost = IntArray(len0)
        for (i in 0 until len0) cost[i] = i
        for (j in 1 until len1) {
            newcost[0] = j
            for (i in 1 until len0) {
                val match = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                val costReplace = cost[i - 1] + match
                val costInsert = cost[i] + 1
                val costDelete = newcost[i - 1] + 1
                newcost[i] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newcost
            newcost = swap
        }
        return cost[len0 - 1]
    }

}