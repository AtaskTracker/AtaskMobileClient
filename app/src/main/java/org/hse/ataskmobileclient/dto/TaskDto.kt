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
        @SerializedName("uuid")
        val uuid: String?,  // can be null for newly created tasks

        @SerializedName("summary")
        val summary: String,

        @SerializedName("description")
        val description: String,

        @SerializedName("photo")
        val photo: String? = null,
//        val status: Status? = null,

        @SerializedName("status")
        val status: String? = null,
//        val date: Date? = null,

        @SerializedName("date")
        val dueDate: String? = null,

        @SerializedName("participants")
        val participants: ArrayList<String>?,

        @SerializedName("labels")
        val labels: ArrayList<LabelDto>?
)