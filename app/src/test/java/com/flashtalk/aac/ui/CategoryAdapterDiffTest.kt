package com.flashtalk.aac.ui

import com.flashtalk.aac.data.Category
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryAdapterDiffTest {

    private val callback = CategoryAdapter.CategoryDiffCallback()
    private val category = Category(id = 1, name = "Animals", icon = "🐶", color = "#FF9F43")

    @Test
    fun `same id is the same item even if other fields differ`() {
        val renamed = category.copy(name = "Pets")
        assertTrue(callback.areItemsTheSame(category, renamed))
    }

    @Test
    fun `different id is a different item`() {
        val other = category.copy(id = 2)
        assertFalse(callback.areItemsTheSame(category, other))
    }

    @Test
    fun `identical categories have the same contents`() {
        assertTrue(callback.areContentsTheSame(category, category.copy()))
    }

    @Test
    fun `a renamed category does not have the same contents`() {
        assertFalse(callback.areContentsTheSame(category, category.copy(name = "Pets")))
    }
}
