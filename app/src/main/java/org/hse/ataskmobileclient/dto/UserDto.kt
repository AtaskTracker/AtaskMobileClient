package org.hse.ataskmobileclient.dto

import com.google.gson.annotations.SerializedName

class UserDto(
    @SerializedName("uuid")
    val uuid : String?,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("picture_url")
    val pictureUrl: String
)