package ru.otus.wishlist.storage

import kotlinx.serialization.Serializable

@Serializable
data class SessionPreferences(val sessionId: String)
