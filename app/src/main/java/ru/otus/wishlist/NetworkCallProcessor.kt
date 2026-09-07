package ru.otus.wishlist

import android.content.SharedPreferences
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import ru.otus.wishlist.storage.forceLogout
import java.io.IOException
import javax.inject.Inject

@ActivityRetainedScoped
class NetworkCallProcessor @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val cache: WizardCache
) {

    suspend fun <T> call(block: suspend () -> Response<T>): Result<T> =
        runCatching {
            val response = withContext(Dispatchers.IO) {
                block()
            }
            when {
                response.code() == 200 -> response.body() ?: throw IOException()
                response.code() == 401 -> {
                    cache.clear()
                    sharedPreferences.forceLogout()
                    throw SecurityException()
                }
                else -> throw IOException()
            }
        }
}