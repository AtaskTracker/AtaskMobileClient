package org.hse.ataskmobileclient.dto

import java.util.*

import com.google.gson.annotations.SerializedName

enum class Status {
        @SerializedName("done")
        DONE,

        @SerializedName("not done")
        NOT_DONE
}

class TaskDto(
        val id: UUID?,  // can be null for newly created tasks
        val summary: String,
        val description: String,
        val photo: String? = null,
//        val status: Status? = null,
        val status: String? = null,
//        val date: Date? = null,
        val date: String? = null,
        val members: ArrayList<String>?,
        val labels: ArrayList<LabelDto>
)