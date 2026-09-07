package ru.otus.wishlist.recyclerview.gifts

import androidx.recyclerview.widget.DiffUtil

class GiftsItemCallback : DiffUtil.ItemCallback<GiftsItem>() {

    override fun areItemsTheSame(oldItem: GiftsItem, newItem: GiftsItem): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: GiftsItem, newItem: GiftsItem): Boolean =
        oldItem.id == newItem.id
}