package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.gson.jsonBody
import com.google.gson.Gson
import org.hse.ataskmobileclient.dto.AddedPhotoDto
import org.hse.ataskmobileclient.dto.GoogleImageDto
import org.hse.ataskmobileclient.models.Urls

class PhotosApi {

    private val gson = Gson()

    suspend fun postPhotoAsync(imageDto: GoogleImageDto) : AddedPhotoDto? {
        val postPhotoUrl = Urls().getGooglePhotoUrl()

        val (_, _, result) = Fuel
            .post(postPhotoUrl)
            .jsonBody(imageDto)
            .awaitStringResponseResult()

        return result.fold(
            success = { data -> gson.fromJson(data, AddedPhotoDto::class.java) },
            failure = { error ->
                Log.e(TAG, "Error posting photo on Google Cloud Storage: ${error.message}")
                null
            }
        )
    }

    companion object {
        private val TAG = "PhotosApi"
    }
}