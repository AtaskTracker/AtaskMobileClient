package org.hse.ataskmobileclient.dto

import com.google.gson.annotations.SerializedName

class AddedPhotoDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("url")
    val url: String,
)