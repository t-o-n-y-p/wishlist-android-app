package ru.otus.wishlist.recyclerview.gifts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.ListAdapter
import ru.otus.wishlist.R

class GiftsItemAdapter(
    private val onCheckedChange: (GiftsItem, Int) -> Unit,
    private val onEditButtonClicked: (GiftsItem, Int) -> Unit,
    private val onDeleteButtonClicked: (GiftsItem) -> Unit,
    private val onBind: (GiftsItem, Group, Group, Group, SwitchCompat) -> Unit
) : ListAdapter<GiftsItem, GiftsItemViewHolder>(GiftsItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        GiftsItemViewHolder(
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_gifts_info, parent, false),
            onCheckedChange = onCheckedChange,
            onEditButtonClicked = onEditButtonClicked,
            onDeleteButtonClicked = onDeleteButtonClicked,
            onBind = onBind
        )

    override fun onBindViewHolder(holder: GiftsItemViewHolder, position: Int) =
        holder.bind(getItem(position))
}