package com.flashtalk.aac.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MascotArtTest {

    @Test
    fun `a simple pose name becomes a prefixed drawable name`() {
        assertEquals("mascot_happy", MascotArt.resourceNameFor("happy"))
    }

    @Test
    fun `pose names are lowercased`() {
        assertEquals("mascot_distressed", MascotArt.resourceNameFor("Distressed"))
    }

    @Test
    fun `spaces become underscores`() {
        assertEquals("mascot_too_loud", MascotArt.resourceNameFor("too loud"))
    }

    @Test
    fun `punctuation is stripped to underscores and not duplicated`() {
        assertEquals("mascot_i_m_lost", MascotArt.resourceNameFor("I'm   lost!!"))
    }

    @Test
    fun `leading and trailing whitespace is trimmed`() {
        assertEquals("mascot_calm", MascotArt.resourceNameFor("  calm  "))
    }

    @Test
    fun `a blank pose resolves to no resource name`() {
        assertNull(MascotArt.resourceNameFor(""))
        assertNull(MascotArt.resourceNameFor("   "))
    }

    @Test
    fun `no character or season falls back to the plain pose name`() {
        assertEquals(listOf("mascot_happy"), MascotArt.resourceNameCandidates("", "", "happy"))
    }

    @Test
    fun `a character alone adds a character-specific candidate before the plain fallback`() {
        assertEquals(
            listOf("mascot_dino_happy", "mascot_happy"),
            MascotArt.resourceNameCandidates("dino", "", "happy")
        )
    }

    @Test
    fun `character and season together try the most specific name first`() {
        assertEquals(
            listOf("mascot_dino_halloween_happy", "mascot_dino_happy", "mascot_happy"),
            MascotArt.resourceNameCandidates("dino", "halloween", "happy")
        )
    }

    @Test
    fun `a season without a character is ignored, since there's nothing to combine it with`() {
        assertEquals(listOf("mascot_happy"), MascotArt.resourceNameCandidates("", "halloween", "happy"))
    }

    @Test
    fun `character and season names are sanitised the same way pose names are`() {
        val candidates = MascotArt.resourceNameCandidates("Zombie Fox", "Christmas!", "excited")
        assertTrue(candidates.contains("mascot_zombie_fox_christmas_excited"))
    }

    @Test
    fun `a blank pose produces no candidates at all, regardless of character or season`() {
        assertEquals(emptyList<String>(), MascotArt.resourceNameCandidates("dino", "halloween", ""))
    }
}

