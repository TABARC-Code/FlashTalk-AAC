package com.flashtalk.aac.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.flashtalk.aac.R
import com.flashtalk.aac.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A single-category "speak from the home screen" widget. Deliberately only
 * offers the shared vocabulary (Category.profileId == 0L) at configuration
 * time — a widget lives on the home screen, not inside any one profile's
 * session, so the vocabulary every profile can see is the only stable
 * choice (CLAUDE.md invariant 13/14).
 */
class NeedsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { appWidgetId -> updateWidget(context, appWidgetManager, appWidgetId) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { deleteCategoryFor(context, it) }
    }

    companion object {
        private const val PREFS_NAME = "FlashTalkWidgetPrefs"
        private const val KEY_PREFIX_CATEGORY = "widget_category_"
        private const val NO_CATEGORY = -1L

        fun categoryIdFor(context: Context, appWidgetId: Int): Long? {
            val stored = prefs(context).getLong(KEY_PREFIX_CATEGORY + appWidgetId, NO_CATEGORY)
            return stored.takeIf { it != NO_CATEGORY }
        }

        fun saveCategoryFor(context: Context, appWidgetId: Int, categoryId: Long) {
            prefs(context).edit().putLong(KEY_PREFIX_CATEGORY + appWidgetId, categoryId).apply()
        }

        fun deleteCategoryFor(context: Context, appWidgetId: Int) {
            prefs(context).edit().remove(KEY_PREFIX_CATEGORY + appWidgetId).apply()
        }

        private fun prefs(context: Context) =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        suspend fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_needs)
            val categoryId = categoryIdFor(context, appWidgetId)

            if (categoryId == null) {
                views.setTextViewText(R.id.widgetTitle, context.getString(R.string.widget_not_configured))
                views.setViewVisibility(R.id.widgetGrid, View.GONE)
                views.setViewVisibility(R.id.widgetEmptyView, View.VISIBLE)
                views.setTextViewText(R.id.widgetEmptyView, context.getString(R.string.widget_not_configured_hint))
                views.setOnClickPendingIntent(R.id.widgetEmptyView, configPendingIntent(context, appWidgetId))
                views.setOnClickPendingIntent(R.id.widgetTitle, configPendingIntent(context, appWidgetId))
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            val category = AppDatabase.getDatabase(context).categoryDao().getCategoryById(categoryId)
            views.setTextViewText(R.id.widgetTitle, "${category?.icon.orEmpty()} ${category?.name.orEmpty()}".trim())
            views.setViewVisibility(R.id.widgetGrid, View.VISIBLE)
            views.setTextViewText(R.id.widgetEmptyView, context.getString(R.string.widget_no_cards))

            // A unique data Uri per widget id, otherwise RemoteViewsService
            // can't tell two configured widgets' adapters apart (Intent
            // equality for services is action+data+component, not extras).
            val serviceIntent = Intent(context, NeedsWidgetRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse("flashtalk-widget://widget/$appWidgetId")
            }
            views.setRemoteAdapter(R.id.widgetGrid, serviceIntent)
            views.setEmptyView(R.id.widgetGrid, R.id.widgetEmptyView)

            // Collection widgets (GridView/ListView backed by a
            // RemoteViewsService) require the template PendingIntent to be
            // mutable, so each item's fill-in Intent can merge in — don't
            // "fix" this to FLAG_IMMUTABLE, it'll silently break every tap.
            val tapPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                Intent(context, WidgetTapReceiver::class.java),
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setPendingIntentTemplate(R.id.widgetGrid, tapPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetGrid)
        }

        private fun configPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, com.flashtalk.aac.ui.WidgetConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
