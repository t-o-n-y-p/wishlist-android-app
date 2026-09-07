package ru.otus.wishlist.recyclerview.wishlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.ListAdapter
import ru.otus.wishlist.R

class WishlistsItemAdapter(
    private val onItemClicked: (WishlistsItem, Int) -> Unit,
    private val onEditButtonClicked: (WishlistsItem, Int) -> Unit,
    private val onDeleteButtonClicked: (WishlistsItem) -> Unit,
    private val onBind: (WishlistsItem, Group, Group) -> Unit
) : ListAdapter<WishlistsItem, WishlistsItemViewHolder>(WishlistsItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WishlistsItemViewHolder(
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_wishlists_info, parent, false),
            onItemClicked = onItemClicked,
            onEditButtonClicked = onEditButtonClicked,
            onDeleteButtonClicked = onDeleteButtonClicked,
            onBind = onBind
        )

    override fun onBindViewHolder(holder: WishlistsItemViewHolder, position: Int) =
        holder.bind(getItem(position))
}