package com.flashtalk.aac.widget

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers the widget-id -> categoryId SharedPreferences mapping — needs a
 * real Context for getSharedPreferences, hence Robolectric rather than
 * plain JUnit (same reasoning as AppRepositoryTest).
 */
@RunWith(RobolectricTestRunner::class)
class NeedsWidgetProviderTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `an unconfigured widget has no stored category`() {
        assertNull(NeedsWidgetProvider.categoryIdFor(context, 101))
    }

    @Test
    fun `saving a category makes it retrievable for that widget id`() {
        NeedsWidgetProvider.saveCategoryFor(context, 202, 42L)

        assertEquals(42L, NeedsWidgetProvider.categoryIdFor(context, 202))
    }

    @Test
    fun `different widget ids don't share stored categories`() {
        NeedsWidgetProvider.saveCategoryFor(context, 303, 1L)
        NeedsWidgetProvider.saveCategoryFor(context, 304, 2L)

        assertEquals(1L, NeedsWidgetProvider.categoryIdFor(context, 303))
        assertEquals(2L, NeedsWidgetProvider.categoryIdFor(context, 304))
    }

    @Test
    fun `deleting a widget's stored category clears it`() {
        NeedsWidgetProvider.saveCategoryFor(context, 405, 7L)

        NeedsWidgetProvider.deleteCategoryFor(context, 405)

        assertNull(NeedsWidgetProvider.categoryIdFor(context, 405))
    }
}
