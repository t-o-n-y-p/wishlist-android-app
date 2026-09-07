package ru.otus.wishlist.recyclerview.users

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.otus.wishlist.R

class UsersItemViewHolder(
    view: View,
    private val onItemClicked: (UsersItem) -> Unit = { _ -> }
) : RecyclerView.ViewHolder(view) {

    private val usernameTextView: TextView by lazy {
        itemView.findViewById(R.id.username)
    }

    fun bind(item: UsersItem) {
        usernameTextView.text = item.username
        itemView.setOnClickListener {
            onItemClicked(item)
        }
    }
}