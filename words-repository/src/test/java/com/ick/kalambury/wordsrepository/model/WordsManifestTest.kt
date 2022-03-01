package com.ick.kalambury.wordsrepository.model

import com.ick.kalambury.wordsrepository.Usage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordsManifestTest {

    @Test
    fun `assets set is not new and not updated`() {
        val set = TestData.showingOnlySet

        assertFalse(set.isNew)
        assertFalse(set.isUpdated)
    }

    @Test
    fun `local set is new and not updated`() {
        val set = TestData.newlyAddedRemoteSet

        assertTrue(set.isNew)
        assertFalse(set.isUpdated)
    }

    @Test
    fun `local set is updated and not new`() {
        val set = TestData.newlyUpdatedRemoteSet

        assertFalse(set.isNew)
        assertTrue(set.isUpdated)
    }

    @Test
    fun `local set is not updated anymore and not new`() {
        val set = TestData.earlierUpdatedRemoteSet

        assertFalse(set.isNew)
        assertFalse(set.isUpdated)
    }

    @Test
    fun `showing only default set`() {
        val set = TestData.showingOnlySet

        assertTrue(set.isEligible(Usage.SHOWING))
        assertFalse(set.isEligible(Usage.DRAWING))
        assertTrue(set.isDefault(Usage.SHOWING))
        assertFalse(set.isDefault(Usage.DRAWING))
    }

    @Test
    fun `showing and drawing default set`() {
        val set = TestData.allOptionsDefaultSet

        assertTrue(set.isEligible(Usage.SHOWING))
        assertTrue(set.isEligible(Usage.DRAWING))
        assertTrue(set.isDefault(Usage.SHOWING))
        assertTrue(set.isDefault(Usage.DRAWING))
    }

}