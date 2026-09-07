package ru.otus.wishlist.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer

inline fun <reified T> SharedPreferences.get(): T {
    val result = getString(T::class.simpleName, null) ?: "{}"
    return Json.decodeFromString<T>(result)
}

inline fun <reified T> SharedPreferences.put(value: T) =
    edit(commit = true) {
        putString(
            T::class.simpleName,
            Json.encodeToString(EmptySerializersModule().serializer(), value))
    }

fun SharedPreferences.logout() =
    put(UserPreferences(logoutEvent = UserPreferences.LogoutEvent.LOGOUT))

fun SharedPreferences.forceLogout() =
    put(UserPreferences(logoutEvent = UserPreferences.LogoutEvent.FORCE_LOGOUT))
