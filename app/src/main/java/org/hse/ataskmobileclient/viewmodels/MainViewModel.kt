package org.hse.ataskmobileclient.viewmodels

import android.app.DownloadManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.telephony.mbms.DownloadRequest
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch

class MainViewModel(private val downloadManager: DownloadManager)
    : ViewModel()
{
    fun loadProfilePicture(ivProfilePicture: ImageView, profilePhotoUrl : String) {
        val downloadUri = Uri.parse(profilePhotoUrl)
        Glide.with(ivProfilePicture).load(downloadUri).into(ivProfilePicture)
    }
}