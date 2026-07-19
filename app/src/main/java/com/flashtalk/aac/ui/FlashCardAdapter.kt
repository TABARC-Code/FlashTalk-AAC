package com.flashtalk.aac.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.FlashCard
import java.io.File

class FlashCardAdapter(
    private val onCardClick: (FlashCard) -> Unit,
    // Null when Edit mode is off — no long-click listener attached at all.
    // Mutable so CategoryActivity can flip it after Settings changes Edit
    // mode; call notifyDataSetChanged() afterwards.
    var onCardLongClick: ((FlashCard) -> Unit)? = null
) : ListAdapter<FlashCard, FlashCardAdapter.FlashCardViewHolder>(FlashCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard, parent, false)
        return FlashCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashCardViewHolder, position: Int) {
        holder.bind(getItem(position), onCardClick, onCardLongClick)
    }

    class FlashCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val context: Context = itemView.context
        private val cardView: MaterialCardView = itemView as MaterialCardView
        private val imageView: ImageView = itemView.findViewById(R.id.cardImage)
        private val emojiView: TextView = itemView.findViewById(R.id.cardEmoji)
        private val textView: TextView = itemView.findViewById(R.id.cardText)
        private val urgentStrokeWidth = (2 * context.resources.displayMetrics.density).toInt()

        fun bind(flashCard: FlashCard, onClick: (FlashCard) -> Unit, onLongClick: ((FlashCard) -> Unit)?) {
            textView.text = flashCard.text
            itemView.contentDescription =
                if (flashCard.priority == "urgent") "Urgent: ${flashCard.text}" else flashCard.text

            // Urgent cards (Stop, Emergency, Seizure warning, ...) get a
            // visible red border so they're quick to spot under stress,
            // regardless of which category colour they sit in.
            if (flashCard.priority == "urgent") {
                cardView.strokeWidth = urgentStrokeWidth
                cardView.strokeColor = ContextCompat.getColor(context, R.color.urgentAccent)
            } else {
                cardView.strokeWidth = 0
            }

            val photoFile = flashCard.imagePath.takeIf { it.isNotBlank() }?.let { File(it) }
            if (flashCard.isCustom && photoFile?.exists() == true) {
                imageView.visibility = View.VISIBLE
                emojiView.visibility = View.GONE
                Glide.with(context)
                    .load(photoFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageView)
            } else {
                // Seed (and image-less imported) cards render their emoji glyph
                // directly — see BACKLOG.md item 1 and FlashCard.emoji.
                imageView.visibility = View.GONE
                emojiView.visibility = View.VISIBLE
                emojiView.text = flashCard.emoji.ifBlank { "🗂️" }
            }

            // Click handling only — press feedback is a StateListAnimator on
            // the card root in item_flashcard.xml, not a manual touch
            // listener, so TalkBack's synthesized clicks work correctly
            // (BACKLOG.md item 10).
            itemView.setOnClickListener { onClick(flashCard) }
            if (onLongClick != null) {
                itemView.setOnLongClickListener {
                    onLongClick(flashCard)
                    true
                }
            } else {
                itemView.setOnLongClickListener(null)
                itemView.isLongClickable = false
            }
        }
    }

    // internal, not private: FlashCardDiffCallbackTest exercises this directly.
    internal class FlashCardDiffCallback : DiffUtil.ItemCallback<FlashCard>() {
        override fun areItemsTheSame(oldItem: FlashCard, newItem: FlashCard) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FlashCard, newItem: FlashCard) = oldItem == newItem
    }
}
