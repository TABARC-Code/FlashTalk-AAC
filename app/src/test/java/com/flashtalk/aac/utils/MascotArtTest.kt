package com.flashtalk.aac.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
