package ru.otus.wishlist.recyclerview.users

import androidx.recyclerview.widget.DiffUtil

class UsersItemCallback : DiffUtil.ItemCallback<UsersItem>() {

    override fun areItemsTheSame(oldItem: UsersItem, newItem: UsersItem): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: UsersItem, newItem: UsersItem): Boolean =
        oldItem.id == newItem.id
}