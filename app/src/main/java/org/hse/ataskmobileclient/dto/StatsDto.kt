package org.hse.ataskmobileclient.dto

import com.google.gson.annotations.SerializedName

class StatsDto(

    @SerializedName("total")
    val total : Int,

    @SerializedName("done")
    val done: Int,
)