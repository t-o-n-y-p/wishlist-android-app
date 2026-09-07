package ru.otus.wishlist.recyclerview.wishlists

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import ru.otus.wishlist.R

class WishlistsItemViewHolder(
    view: View,
    private val onItemClicked: (WishlistsItem, Int) -> Unit = { _, _ -> },
    private val onEditButtonClicked: (WishlistsItem, Int) -> Unit = { _, _ -> },
    private val onDeleteButtonClicked: (WishlistsItem) -> Unit = { _ -> },
    private val onBind: (WishlistsItem, Group, Group) -> Unit = { _, _, _ -> }
) : RecyclerView.ViewHolder(view) {

    private val titleTextView: TextView by lazy {
        itemView.findViewById(R.id.title)
    }
    private val descriptionTextView: TextView by lazy {
        itemView.findViewById(R.id.description)
    }
    private val editButton: Button by lazy {
        itemView.findViewById(R.id.edit_button)
    }
    private val deleteButton: Button by lazy {
        itemView.findViewById(R.id.delete_button)
    }
    private val actionGroup: Group by lazy {
        itemView.findViewById(R.id.action_group)
    }
    private val loadingGroup: Group by lazy {
        itemView.findViewById(R.id.loading_group)
    }

    fun bind(item: WishlistsItem) {
        onBind(item, actionGroup, loadingGroup)
        titleTextView.text = item.title
        descriptionTextView.text = item.description
        itemView.setOnClickListener {
            onItemClicked(item, absoluteAdapterPosition)
        }
        editButton.setOnClickListener {
            onEditButtonClicked(item, absoluteAdapterPosition)
        }
        deleteButton.setOnClickListener {
            onDeleteButtonClicked(item)
        }
    }
}