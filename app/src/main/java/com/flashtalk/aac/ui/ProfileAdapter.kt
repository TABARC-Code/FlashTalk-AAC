package com.flashtalk.aac.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.Profile

class ProfileAdapter(
    private val onProfileClick: (Profile) -> Unit,
    // Null when Edit mode is off — same not-attached-at-all pattern as
    // CategoryAdapter/FlashCardAdapter (CLAUDE.md invariant 9).
    var onProfileLongClick: ((Profile) -> Unit)? = null
) : ListAdapter<Profile, ProfileAdapter.ProfileViewHolder>(ProfileDiffCallback()) {

    // Set by ProfileActivity from the stored preference; changing it just
    // moves which row shows the current-profile mark, no data change.
    var currentProfileId: Long = -1L
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = getItem(position)
        holder.bind(profile, profile.id == currentProfileId, onProfileClick, onProfileLongClick)
    }

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconText: TextView = itemView.findViewById(R.id.profileIcon)
        private val nameText: TextView = itemView.findViewById(R.id.profileName)
        private val currentMark: View = itemView.findViewById(R.id.profileCurrentMark)

        fun bind(profile: Profile, isCurrent: Boolean, onClick: (Profile) -> Unit, onLongClick: ((Profile) -> Unit)?) {
            iconText.text = profile.icon
            nameText.text = profile.name
            itemView.contentDescription = profile.name
            currentMark.visibility = if (isCurrent) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onClick(profile) }
            if (onLongClick != null) {
                itemView.setOnLongClickListener {
                    onLongClick(profile)
                    true
                }
            } else {
                itemView.setOnLongClickListener(null)
                itemView.isLongClickable = false
            }
        }
    }

    // internal, not private: ProfileAdapterDiffTest exercises this directly.
    internal class ProfileDiffCallback : DiffUtil.ItemCallback<Profile>() {
        override fun areItemsTheSame(oldItem: Profile, newItem: Profile) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Profile, newItem: Profile) = oldItem == newItem
    }
}
