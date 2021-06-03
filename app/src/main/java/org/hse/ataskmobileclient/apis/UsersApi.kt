package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.google.gson.Gson
import org.hse.ataskmobileclient.dto.UserDto
import org.hse.ataskmobileclient.models.Urls

class UsersApi {
    suspend fun getUserByEmail(token: String, email: String) : UserDto? {

        val usersUrl = Urls().getUserUrl()
        val (request, response, result) = Fuel
            .get(usersUrl, listOf("email" to email))
            .header("Authorization", "Bearer $token")
            .awaitStringResponseResult()

        return result.fold(
            { success -> Gson().fromJson(success, UserDto::class.java) as UserDto  },
            { error ->
                Log.e(TAG, "Ошибка при запросе пользователя по почте, " +
                        "${response.statusCode}, ${error.message}")
                return null
            }
        )
    }

    companion object {
        private const val TAG = "UsersApi"
    }
}