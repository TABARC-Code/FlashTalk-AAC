package com.flashtalk.aac.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.flashtalk.aac.R
import com.flashtalk.aac.data.AppDatabase
import com.flashtalk.aac.data.FlashCard
import kotlinx.coroutines.runBlocking

class NeedsWidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NeedsWidgetRemoteViewsFactory(applicationContext, intent)
    }
}

/**
 * RemoteViewsFactory methods run on a background thread the widget host
 * already provides for exactly this purpose (unlike AppWidgetProvider's
 * own callbacks, which run on the main thread) — blocking Room calls here
 * are the documented, expected pattern, not a shortcut.
 */
class NeedsWidgetRemoteViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
    )
    private var cards: List<FlashCard> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val categoryId = NeedsWidgetProvider.categoryIdFor(context, appWidgetId)
        cards = if (categoryId == null) {
            emptyList()
        } else {
            runBlocking { AppDatabase.getDatabase(context).flashCardDao().getEnabledCardsByCategorySync(categoryId) }
        }
    }

    override fun onDestroy() {
        cards = emptyList()
    }

    override fun getCount(): Int = cards.size

    override fun getViewAt(position: Int): RemoteViews {
        val card = cards[position]
        return RemoteViews(context.packageName, R.layout.widget_needs_item).apply {
            setTextViewText(R.id.widgetCardEmoji, card.emoji.ifBlank { "🗣️" })
            setTextViewText(R.id.widgetCardLabel, card.text)
            setOnClickFillInIntent(
                R.id.widgetCardRoot,
                Intent().putExtra(WidgetTapReceiver.EXTRA_SPEECH_TEXT, card.speechText)
            )
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = cards[position].id
    override fun hasStableIds(): Boolean = true
}
