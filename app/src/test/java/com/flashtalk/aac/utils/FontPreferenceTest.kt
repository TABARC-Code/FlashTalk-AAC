package com.flashtalk.aac.utils

import com.flashtalk.aac.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FontPreferenceTest {

    @Test
    fun `default resolves to no font override`() {
        assertNull(FontPreference.fontResIdFor(FontPreference.DEFAULT))
    }

    @Test
    fun `an unrecognised value resolves to no font override`() {
        assertNull(FontPreference.fontResIdFor("something_unexpected"))
    }

    @Test
    fun `dyslexia-friendly resolves to a font resource`() {
        assertEquals(R.font.opendyslexic, FontPreference.fontResIdFor(FontPreference.DYSLEXIA_FRIENDLY))
    }

    @Test
    fun `easy read resolves to a font resource`() {
        assertEquals(R.font.atkinson_hyperlegible, FontPreference.fontResIdFor(FontPreference.EASY_READ))
    }

    @Test
    fun `dyslexia-friendly and easy read resolve to different fonts`() {
        assertEquals(false, FontPreference.fontResIdFor(FontPreference.DYSLEXIA_FRIENDLY) ==
            FontPreference.fontResIdFor(FontPreference.EASY_READ))
    }
}
