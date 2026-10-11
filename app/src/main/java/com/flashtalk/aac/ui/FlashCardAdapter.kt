package com.flashtalk.aac.ui

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.FlashCard
import com.flashtalk.aac.utils.MascotArt
import java.io.File

class FlashCardAdapter(
    private val onCardClick: (FlashCard) -> Unit,
    // Null when Edit mode is off — no long-click listener attached at all.
    // Mutable so CategoryActivity can flip it after Settings changes Edit
    // mode; call notifyDataSetChanged() afterwards.
    var onCardLongClick: ((FlashCard) -> Unit)? = null,
    // Off by default, matching Settings' Mascot mode default (CLAUDE.md
    // invariant 15). Mutable for the same reason as onCardLongClick above.
    var mascotModeEnabled: Boolean = false,
    // Off by default (CLAUDE.md invariant 16) — takes priority over
    // everything else below: a custom photo, mascot art, even the emoji
    // fallback. "Image free" means image free, not "image free except for
    // whatever else happens to be set."
    var pureTextMode: Boolean = false,
    // Null means "use whatever the view already has" — no override.
    var customTypeface: Typeface? = null
) : ListAdapter<FlashCard, FlashCardAdapter.FlashCardViewHolder>(FlashCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard, parent, false)
        return FlashCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashCardViewHolder, position: Int) {
        holder.bind(getItem(position), mascotModeEnabled, pureTextMode, customTypeface, onCardClick, onCardLongClick)
    }

    class FlashCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val context: Context = itemView.context
        private val cardView: MaterialCardView = itemView as MaterialCardView
        private val imageView: ImageView = itemView.findViewById(R.id.cardImage)
        private val emojiView: TextView = itemView.findViewById(R.id.cardEmoji)
        private val textView: TextView = itemView.findViewById(R.id.cardText)
        private val urgentStrokeWidth = (2 * context.resources.displayMetrics.density).toInt()

        fun bind(
            flashCard: FlashCard,
            mascotModeEnabled: Boolean,
            pureTextMode: Boolean,
            customTypeface: Typeface?,
            onClick: (FlashCard) -> Unit,
            onLongClick: ((FlashCard) -> Unit)?
        ) {
            textView.text = flashCard.text
            itemView.contentDescription =
                if (flashCard.priority == "urgent") "Urgent: ${flashCard.text}" else flashCard.text

            if (customTypeface != null) {
                textView.typeface = customTypeface
            }

            // Urgent cards (Stop, Emergency, Seizure warning, ...) get a
            // visible red border so they're quick to spot under stress,
            // regardless of which category colour they sit in.
            if (flashCard.priority == "urgent") {
                cardView.strokeWidth = urgentStrokeWidth
                cardView.strokeColor = ContextCompat.getColor(context, R.color.urgentAccent)
            } else {
                cardView.strokeWidth = 0
            }

            if (pureTextMode) {
                imageView.visibility = View.GONE
                emojiView.visibility = View.GONE
                setTextFillsCard(true)
            } else {
                setTextFillsCard(false)
                val photoFile = flashCard.imagePath.takeIf { it.isNotBlank() }?.let { File(it) }
                val mascotResId = if (mascotModeEnabled) mascotDrawableResId(flashCard.mascotPose) else null

                when {
                    flashCard.isCustom && photoFile?.exists() == true -> {
                        imageView.visibility = View.VISIBLE
                        emojiView.visibility = View.GONE
                        Glide.with(context)
                            .load(photoFile)
                            .centerCrop()
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(imageView)
                    }
                    // Mascot mode, and this card has a pose with matching
                    // artwork actually bundled — see MascotArt. Every seed card
                    // ships with mascotPose blank, so this branch does nothing
                    // yet on an unmodified install; it only activates once both
                    // a card's mascotPose and a matching drawable exist.
                    mascotResId != null -> {
                        imageView.visibility = View.VISIBLE
                        emojiView.visibility = View.GONE
                        imageView.setImageResource(mascotResId)
                    }
                    else -> {
                        // Seed (and image-less imported) cards render their emoji glyph
                        // directly — see BACKLOG.md item 1 and FlashCard.emoji.
                        imageView.visibility = View.GONE
                        emojiView.visibility = View.VISIBLE
                        emojiView.text = flashCard.emoji.ifBlank { "🗂️" }
                    }
                }
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

        // Views get recycled, so this has to run every bind (not just when
        // pure text mode turns on) — a holder last bound in pure-text mode
        // must have its weight/height reset for the next, non-pure-text card.
        private fun setTextFillsCard(fillsCard: Boolean) {
            val params = textView.layoutParams as LinearLayout.LayoutParams
            if (fillsCard) {
                params.height = 0
                params.weight = 1f
                textView.gravity = android.view.Gravity.CENTER
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                params.weight = 0f
                textView.gravity = android.view.Gravity.CENTER
            }
            textView.layoutParams = params
        }

        // Resources.getIdentifier is a reflection-based lookup — fine here
        // since it only runs once per card bind, not in a hot loop. Returns
        // null (not 0) when nothing matches, so callers don't need to know
        // Android's "0 means not found" convention.
        private fun mascotDrawableResId(pose: String): Int? {
            val name = MascotArt.resourceNameFor(pose) ?: return null
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            return resId.takeIf { it != 0 }
        }
    }

    // internal, not private: FlashCardDiffCallbackTest exercises this directly.
    internal class FlashCardDiffCallback : DiffUtil.ItemCallback<FlashCard>() {
        override fun areItemsTheSame(oldItem: FlashCard, newItem: FlashCard) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FlashCard, newItem: FlashCard) = oldItem == newItem
    }
}
