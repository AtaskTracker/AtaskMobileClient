package org.hse.ataskmobileclient.dto

import com.google.gson.annotations.SerializedName

class GoogleImageDto(
    @SerializedName("name")
    val name : String,

    @SerializedName("payload")
    val photoBase64: String
)