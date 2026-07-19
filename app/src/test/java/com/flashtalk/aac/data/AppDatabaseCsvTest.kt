package com.flashtalk.aac.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers AppDatabase.parseCsvLine — the vocabulary seed data depends on it
 * getting quoted, comma-containing category names right (e.g. "Health,
 * Feelings & Emergencies"), which is exactly the kind of thing that breaks
 * silently under a later refactor with nothing here to catch it.
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
}
