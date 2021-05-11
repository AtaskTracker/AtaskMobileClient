package org.hse.ataskmobileclient

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val profilePicture: MutableLiveData<Bitmap> = MutableLiveData()

    fun loadProfilePicture(profilePhotoUrl : String) {
        viewModelScope.launch {
            val client = HttpClient()
            val response : String = client.request(profilePhotoUrl)
            client.close()
            val bitmap = BitmapFactory.decodeStream(response.byteInputStream())
            profilePicture.value = bitmap
        }
    }
}