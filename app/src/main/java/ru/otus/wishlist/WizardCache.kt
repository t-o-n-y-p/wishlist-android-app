package ru.otus.wishlist

import dagger.hilt.android.scopes.ActivityRetainedScoped
import ru.otus.wishlist.recyclerview.gifts.GiftsItem
import ru.otus.wishlist.recyclerview.users.UsersItem
import ru.otus.wishlist.recyclerview.wishlists.WishlistsItem
import javax.inject.Inject

@ActivityRetainedScoped
class WizardCache @Inject constructor() {

    var users: MutableList<UsersItem> = mutableListOf()
    var filteredUsers: List<UsersItem> = mutableListOf()
    var currentUser: UsersItem? = null
    var usernameFilter: String = ""
    var wishlists: MutableList<WishlistsItem> = mutableListOf()
    var currentWishlist: WishlistsItem? = null
    var currentWishlistPosition: Int = 0
    var currentGift: GiftsItem? = null
    var currentGiftPosition: Int = 0

    fun clear() {
        users = mutableListOf()
        currentUser = null
        usernameFilter = ""
        wishlists = mutableListOf()
        currentWishlist = null
        currentWishlistPosition = 0
        currentGift = null
        currentGiftPosition = 0
    }
}