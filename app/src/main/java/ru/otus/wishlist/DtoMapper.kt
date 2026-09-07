package ru.otus.wishlist

import dagger.hilt.android.scopes.ActivityRetainedScoped
import ru.otus.wishlist.api.models.Gift
import ru.otus.wishlist.api.models.User
import ru.otus.wishlist.api.models.Wishlist
import ru.otus.wishlist.recyclerview.gifts.GiftsItem
import ru.otus.wishlist.recyclerview.users.UsersItem
import ru.otus.wishlist.recyclerview.wishlists.WishlistsItem
import javax.inject.Inject

@ActivityRetainedScoped
class DtoMapper @Inject constructor() {

    fun mapToWishlistsItem(source: List<Wishlist>): MutableList<WishlistsItem> =
        source.map { mapToWishlistsItem(it) }.toMutableList()

    fun mapToWishlistsItem(source: Wishlist): WishlistsItem =
        WishlistsItem(
            id = source.id.orEmpty(),
            title = source.title.orEmpty(),
            description = source.description.orEmpty(),
            gifts = source.gifts
                ?.map { mapToGiftsItem(it) }
                .orEmpty()
                .toMutableList()
        )

    fun mapToGiftsItem(source: Gift): GiftsItem =
        GiftsItem(
            id = source.id.orEmpty(),
            name = source.name.orEmpty(),
            description = source.description.orEmpty(),
            price = source.price?.toInt() ?: 0,
            reserved = source.reserved ?: false
        )

    fun mapToUsersItem(source: List<User>, filter: (User) -> Boolean): MutableList<UsersItem> =
        source.filter { filter(it) }.map { mapToUsersItem(it) }.toMutableList()

    fun mapToUsersItem(source: User): UsersItem =
        UsersItem(
            id = source.id.orEmpty(),
            username = source.username.orEmpty()
        )
}