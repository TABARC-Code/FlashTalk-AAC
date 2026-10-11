package com.flashtalk.aac.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.Category
import com.flashtalk.aac.utils.FontPreference

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    // Null when Edit mode is off — no long-click listener gets attached at
    // all in that case, not just a listener that happens to do nothing.
    // Mutable (not a constructor val) so MainActivity can flip it after
    // Settings changes Edit mode, without rebuilding the whole adapter —
    // call notifyDataSetChanged() after changing it so bound holders pick
    // the new value up.
    var onCategoryLongClick: ((Category) -> Unit)? = null,
    // Off by default, matching Settings' Pure text mode default (CLAUDE.md
    // invariant 16). Hides the category icon entirely, not just emoji —
    // "image free" means image free.
    var pureTextMode: Boolean = false,
    // Null means "use whatever the view already has" — no override.
    var customTypeface: Typeface? = null
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), pureTextMode, customTypeface, onCategoryClick, onCategoryLongClick)
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.categoryCard)
        private val iconText: TextView = itemView.findViewById(R.id.categoryIcon)
        private val nameText: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(
            category: Category,
            pureTextMode: Boolean,
            customTypeface: Typeface?,
            onClick: (Category) -> Unit,
            onLongClick: ((Category) -> Unit)?
        ) {
            iconText.text = category.icon
            iconText.visibility = if (pureTextMode) View.GONE else View.VISIBLE
            nameText.text = category.name
            itemView.contentDescription = category.name

            if (customTypeface != null) {
                nameText.typeface = customTypeface
            }

            try {
                cardView.setCardBackgroundColor(Color.parseColor(category.color))
            } catch (e: IllegalArgumentException) {
                cardView.setCardBackgroundColor(Color.parseColor("#95E1D3"))
            }

            itemView.setOnClickListener { onClick(category) }
            if (onLongClick != null) {
                itemView.setOnLongClickListener {
                    onLongClick(category)
                    true
                }
            } else {
                itemView.setOnLongClickListener(null)
                itemView.isLongClickable = false
            }
        }
    }

    // internal, not private: CategoryDiffCallbackTest exercises this directly.
    internal class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
    }
}
