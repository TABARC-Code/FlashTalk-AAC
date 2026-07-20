package com.flashtalk.aac.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers AppDatabase's pure companion functions: parseCsvLine (the
 * vocabulary seed data depends on it getting quoted, comma-containing
 * category names right, e.g. "Health, Feelings & Emergencies") and
 * vocabularyAssetNameFor (locale-based seed file selection, BACKLOG.md
 * item 1). Neither touches Context or the filesystem, so no Robolectric
 * needed here.
 */
class AppDatabaseCsvTest {

    @Test
    fun `plain unquoted fields split on comma`() {
        val result = AppDatabase.parseCsvLine("core_social-yes,Yes,Yes,core_social,Core & Social")
        assertEquals(listOf("core_social-yes", "Yes", "Yes", "core_social", "Core & Social"), result)
    }

    @Test
    fun `quoted field containing a comma is not split`() {
        val result = AppDatabase.parseCsvLine(
            """health_feelings_emergency-emergency,Emergency,Emergency,health_feelings_emergency,"Health, Feelings & Emergencies",Red,#E97B75,,urgent,True,103,🚨"""
        )
        assertEquals(12, result.size)
        assertEquals("Health, Feelings & Emergencies", result[4])
    }

    @Test
    fun `empty field between two commas is an empty string, not dropped`() {
        val result = AppDatabase.parseCsvLine("a,,c")
        assertEquals(listOf("a", "", "c"), result)
    }

    @Test
    fun `doubled quotes inside a quoted field unescape to one quote`() {
        // Raw triple-quoted strings can't contain a literal """ run, so this
        // is written with escapes rather than as a """...""" literal — the
        // CSV content itself (an escaped quote landing right before the
        // field's closing quote) is exactly that awkward case.
        val result = AppDatabase.parseCsvLine("a,\"say \"\"hi\"\" now\",c")
        assertEquals(listOf("a", "say \"hi\" now", "c"), result)
    }

    @Test
    fun `trailing empty field is preserved`() {
        val result = AppDatabase.parseCsvLine("a,b,")
        assertEquals(listOf("a", "b", ""), result)
    }

    @Test
    fun `matching locale variant is chosen when bundled`() {
        val result = AppDatabase.vocabularyAssetNameFor(
            "fr",
            setOf("communicards_vocabulary_v1.csv", "communicards_vocabulary_v1_fr.csv")
        )
        assertEquals("communicards_vocabulary_v1_fr.csv", result)
    }

    @Test
    fun `falls back to the English default when no variant is bundled`() {
        val result = AppDatabase.vocabularyAssetNameFor(
            "de",
            setOf("communicards_vocabulary_v1.csv", "communicards_vocabulary_v1_fr.csv")
        )
        assertEquals("communicards_vocabulary_v1.csv", result)
    }

    @Test
    fun `language tag matching is case-insensitive`() {
        val result = AppDatabase.vocabularyAssetNameFor(
            "FR",
            setOf("communicards_vocabulary_v1.csv", "communicards_vocabulary_v1_fr.csv")
        )
        assertEquals("communicards_vocabulary_v1_fr.csv", result)
    }

    @Test
    fun `english locale uses the default file, not a redundant variant`() {
        val result = AppDatabase.vocabularyAssetNameFor(
            "en",
            setOf("communicards_vocabulary_v1.csv", "communicards_vocabulary_v1_fr.csv")
        )
        assertEquals("communicards_vocabulary_v1.csv", result)
    }
}
