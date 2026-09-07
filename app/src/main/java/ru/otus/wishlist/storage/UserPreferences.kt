package ru.otus.wishlist.storage

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    var token: String = "",
    var username: String = "",
    var logoutEvent: LogoutEvent = LogoutEvent.LOGOUT
) {

    fun getAuthHeaderValue() = "Bearer $token"

    fun isLoggedIn() = !isNotLoggedIn()

    fun isNotLoggedIn() = token.isBlank()

    enum class LogoutEvent {
        LOGOUT,
        FORCE_LOGOUT
    }
}
