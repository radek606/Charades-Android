package com.ick.kalambury.words

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WordMatcherTest {

    lateinit var word: Word

    @Before
    fun prepare() {
        word = Word(listOf("kupować kota w worku", "kupić kota w worku"))
    }

    @Test
    fun `first variant is match`() {
        Assert.assertTrue(WordMatcher.matchAnswer(word, "kupować kota w worku").isMatch)
    }

    @Test
    fun `second variant is match`() {
        Assert.assertTrue(WordMatcher.matchAnswer(word, "kupić kota w worku").isMatch)
    }

    @Test
    fun `variant without diacritic marks is match`() {
        Assert.assertTrue(WordMatcher.matchAnswer(word, "kupic kota w worku").isMatch)
    }

    @Test
    fun `single shorter word is close enough`() {
        val result = WordMatcher.matchAnswer(word, "kot")
        Assert.assertFalse(result.isMatch)
        Assert.assertTrue(result.isCloseEnough)
    }

    @Test
    fun `single similar word is close enough`() {
        val result = WordMatcher.matchAnswer(word, "worek")
        Assert.assertFalse(result.isMatch)
        Assert.assertTrue(result.isCloseEnough)
    }

    @Test
    fun `single exact word is close enough`() {
        val result = WordMatcher.matchAnswer(word, "kupować")
        Assert.assertFalse(result.isMatch)
        Assert.assertTrue(result.isCloseEnough)
    }

    @Test
    fun `less words than in variant with one matching is close enough`() {
        val result = WordMatcher.matchAnswer(word, "kupować w sklepie")
        Assert.assertFalse(result.isMatch)
        Assert.assertTrue(result.isCloseEnough)
    }

    @Test
    fun `more words than in variant with one matching is close enough`() {
        val result = WordMatcher.matchAnswer(word, "kupować zwierzę w sklepie zoologicznym")
        Assert.assertFalse(result.isMatch)
        Assert.assertTrue(result.isCloseEnough)
    }

    @Test
    fun `similar words no match`() {
        val custom = Word(listOf("skóra"))
        val result = WordMatcher.matchAnswer(custom, "szkoła")
        Assert.assertFalse(result.isMatch)
        Assert.assertFalse(result.isCloseEnough)
    }

}