package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.gson.jsonBody
import org.hse.ataskmobileclient.dto.AuthDto
import org.hse.ataskmobileclient.models.Urls

class AuthApi {

    suspend fun authWithGoogle (token: String) : Boolean {

        val authDto = AuthDto(token)

        val fuelManager = FuelManager.instance
        fuelManager.timeoutInMillisecond = 30 * 1000

        val authUrl = Urls().getGoogleAuthUrl()
        val (_, _, result) = fuelManager
            .post(authUrl)
            .jsonBody(authDto)
            .awaitStringResponseResult()

        return result.fold(
            success = { true },
            failure = { error ->
                Log.e(TAG, "Error during auth on backend: ${error.message}")
                false
            }
        )
    }

    companion object {
        private const val TAG = "AuthApi"
    }
}