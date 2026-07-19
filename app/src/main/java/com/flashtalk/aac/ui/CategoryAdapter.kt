package com.flashtalk.aac.ui

import android.graphics.Color
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

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    // Null when Edit mode is off — no long-click listener gets attached at
    // all in that case, not just a listener that happens to do nothing.
    // Mutable (not a constructor val) so MainActivity can flip it after
    // Settings changes Edit mode, without rebuilding the whole adapter —
    // call notifyDataSetChanged() after changing it so bound holders pick
    // the new value up.
    var onCategoryLongClick: ((Category) -> Unit)? = null
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), onCategoryClick, onCategoryLongClick)
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.categoryCard)
        private val iconText: TextView = itemView.findViewById(R.id.categoryIcon)
        private val nameText: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(category: Category, onClick: (Category) -> Unit, onLongClick: ((Category) -> Unit)?) {
            iconText.text = category.icon
            nameText.text = category.name
            itemView.contentDescription = category.name

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
