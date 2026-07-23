package com.flashtalk.aac.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.Category
import com.flashtalk.aac.widget.NeedsWidgetProvider
import kotlinx.coroutines.launch

/**
 * Launched automatically by the widget host when NeedsWidgetProvider is
 * placed on a home screen (android:configure in needs_widget_info.xml).
 * Only offers the shared vocabulary (Category.profileId == 0L) — a widget
 * isn't scoped to any one profile, see NeedsWidgetProvider's class doc.
 */
class WidgetConfigActivity : BaseActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_widget_config)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val viewModel = ViewModelProvider(this, WidgetConfigViewModelFactory(application))[WidgetConfigViewModel::class.java]
        val adapter = CategoryAdapter(onCategoryClick = { category -> finishWithCategory(category) })
        findViewById<RecyclerView>(R.id.widgetConfigRecyclerView).apply {
            layoutManager = GridLayoutManager(this@WidgetConfigActivity, 2)
            this.adapter = adapter
            setHasFixedSize(true)
        }

        lifecycleScope.launch {
            adapter.submitList(viewModel.getSharedCategories())
        }
    }

    private fun finishWithCategory(category: Category) {
        NeedsWidgetProvider.saveCategoryFor(this, appWidgetId, category.id)

        lifecycleScope.launch {
            NeedsWidgetProvider.updateWidget(this@WidgetConfigActivity, AppWidgetManager.getInstance(this@WidgetConfigActivity), appWidgetId)
        }

        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
