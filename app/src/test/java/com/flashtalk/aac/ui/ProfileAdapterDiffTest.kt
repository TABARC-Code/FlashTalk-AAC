package com.flashtalk.aac.ui

import com.flashtalk.aac.data.Profile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileAdapterDiffTest {

    private val callback = ProfileAdapter.ProfileDiffCallback()
    private val profile = Profile(id = 1, name = "Alex", icon = "🧑")

    @Test
    fun `same id is the same item even if other fields differ`() {
        val renamed = profile.copy(name = "Sam")
        assertTrue(callback.areItemsTheSame(profile, renamed))
    }

    @Test
    fun `different id is a different item`() {
        val other = profile.copy(id = 2)
        assertFalse(callback.areItemsTheSame(profile, other))
    }

    @Test
    fun `identical profiles have the same contents`() {
        assertTrue(callback.areContentsTheSame(profile, profile.copy()))
    }

    @Test
    fun `a renamed profile does not have the same contents`() {
        assertFalse(callback.areContentsTheSame(profile, profile.copy(name = "Sam")))
    }
}
