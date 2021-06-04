package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import org.hse.ataskmobileclient.dto.AuthDto
import org.hse.ataskmobileclient.dto.AuthResultDto
import org.hse.ataskmobileclient.models.Urls

class AuthApi {

    fun authWithGoogle (token: String, responseHandler: (result: AuthResultDto) -> Unit?) {

        val authDto = AuthDto(token)

        Urls()
            .getGoogleAuthUrl()
            .httpPost()
            .jsonBody(authDto)
            .responseString { _, _, result ->
                this.authResultHandler(result, responseHandler)
            }
    }

    private fun authResultHandler (
        result : Result<String, FuelError>,
        responseHandler: (result: AuthResultDto) -> Unit?
    ) {
        when (result) {
            is Result.Failure -> {
                Log.i("ErrorMsg", result.getException().message ?: "")
                result.getException().stackTrace
                throw Exception(result.getException())
            }
            is Result.Success -> {
                val authResult: AuthResultDto? = Gson().fromJson(result.get(), AuthResultDto::class.java)
                if (authResult != null) {
                    responseHandler.invoke(authResult)
                }
            }
        }
    }

    companion object {
        private const val TAG = "AuthApi"
    }
}