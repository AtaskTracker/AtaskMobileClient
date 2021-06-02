package org.hse.ataskmobileclient.models

import java.util.*

import com.google.gson.annotations.SerializedName

enum class Status {
        @SerializedName("done")
        DONE,

        @SerializedName("not done")
        NOT_DONE
}

class TaskResult(
        val id: UUID,
        val summary: String,
        val description: String,
        val photo: String? = null,
//        val status: Status? = null,
        val status: Status? = null,
        val date: Date? = null,
        val members: ArrayList<String>,
        val labels: ArrayList<Label>
)