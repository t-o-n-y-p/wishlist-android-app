package ru.otus.wishlist.recyclerview.wishlists

import androidx.recyclerview.widget.DiffUtil

class WishlistsItemCallback : DiffUtil.ItemCallback<WishlistsItem>() {

    override fun areItemsTheSame(oldItem: WishlistsItem, newItem: WishlistsItem): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: WishlistsItem, newItem: WishlistsItem): Boolean =
        oldItem.id == newItem.id
}