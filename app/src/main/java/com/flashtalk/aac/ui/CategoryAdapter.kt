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
    private val onCategoryClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), onCategoryClick)
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.categoryCard)
        private val iconText: TextView = itemView.findViewById(R.id.categoryIcon)
        private val nameText: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(category: Category, onClick: (Category) -> Unit) {
            iconText.text = category.icon
            nameText.text = category.name
            itemView.contentDescription = category.name

            try {
                cardView.setCardBackgroundColor(Color.parseColor(category.color))
            } catch (e: IllegalArgumentException) {
                cardView.setCardBackgroundColor(Color.parseColor("#95E1D3"))
            }

            itemView.setOnClickListener { onClick(category) }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
    }
}
