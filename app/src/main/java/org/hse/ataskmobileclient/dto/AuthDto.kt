package org.hse.ataskmobileclient.dto

import com.google.gson.annotations.SerializedName

class AuthDto(
    @SerializedName("id_token")
    val idToken : String
)