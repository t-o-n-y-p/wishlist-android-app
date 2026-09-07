package ru.otus.wishlist.recyclerview.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.otus.wishlist.R

class UsersItemAdapter(
    private val onItemClicked: (UsersItem) -> Unit
) : ListAdapter<UsersItem, UsersItemViewHolder>(UsersItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UsersItemViewHolder(
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_users_info, parent, false),
            onItemClicked = onItemClicked
        )

    override fun onBindViewHolder(holder: UsersItemViewHolder, position: Int) =
        holder.bind(getItem(position))
}