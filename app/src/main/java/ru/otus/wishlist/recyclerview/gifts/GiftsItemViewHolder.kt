package ru.otus.wishlist.recyclerview.gifts

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import ru.otus.wishlist.R

class GiftsItemViewHolder(
    view: View,
    private val onCheckedChange: (GiftsItem, Int) -> Unit = { _, _ -> },
    private val onEditButtonClicked: (GiftsItem, Int) -> Unit = { _, _ -> },
    private val onDeleteButtonClicked: (GiftsItem) -> Unit = { _ -> },
    private val onBind: (GiftsItem, Group, Group, Group, SwitchCompat) -> Unit =
        { _, _, _, _, _ ->
        },
) : RecyclerView.ViewHolder(view) {

    private val titleTextView: TextView by lazy { itemView.findViewById(R.id.title) }
    private val descriptionTextView: TextView by lazy {
        itemView.findViewById(R.id.description)
    }
    private val priceTextView: TextView by lazy { itemView.findViewById(R.id.price) }
    private val editButton: Button by lazy { itemView.findViewById(R.id.edit_button) }
    private val deleteButton: Button by lazy { itemView.findViewById(R.id.delete_button) }
    private val reservedSwitch: SwitchCompat by lazy {
        itemView.findViewById(R.id.reserved)
    }
    private val actionGroup: Group by lazy { itemView.findViewById(R.id.action_group) }
    private val loadingGroup: Group by lazy { itemView.findViewById(R.id.loading_group) }
    private val reservedGroup: Group by lazy {
        itemView.findViewById(R.id.reserved_group)
    }

    fun bind(item: GiftsItem) {
        onBind(item, actionGroup, loadingGroup, reservedGroup, reservedSwitch)
        titleTextView.text = item.name
        descriptionTextView.text = item.description
        priceTextView.text = itemView.context.getString(R.string.price, item.price)
        reservedSwitch.isChecked = item.reserved
        editButton.setOnClickListener {
            onEditButtonClicked(item, absoluteAdapterPosition)
        }
        deleteButton.setOnClickListener { onDeleteButtonClicked(item) }
        reservedSwitch.setOnCheckedChangeListener { _, _ ->
            onCheckedChange(item, absoluteAdapterPosition)
        }
    }
}
