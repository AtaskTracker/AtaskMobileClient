package org.hse.ataskmobileclient.models

import java.util.*

class Urls {
    private val baseUrl = "https://api-atasktracker.herokuapp.com"

    fun getLabelUrl() : String {
        return "$baseUrl/label/"
    }

    fun getTaskUrl(task_id: UUID) : String {
        return "$baseUrl/task/$task_id"
    }

    fun getTaskLabelUrl(task_id: UUID) : String {
        return "$baseUrl/task/label/$task_id"
    }

    fun getTaskUrl() : String {
        return "$baseUrl/task"
    }

    fun getLogoutUrl (): String {
        return "$baseUrl/auth/logout"
    }

    fun getGoogleAuthUrl (): String {
        return "$baseUrl/auth/google"
    }

    fun getStatsUrl() : String {
        return "$baseUrl/stats/done"
    }
}