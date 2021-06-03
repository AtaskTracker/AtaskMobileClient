package org.hse.ataskmobileclient.models

class Urls {
    private val baseUrl = "https://virtserver.swaggerhub.com/thinkingabouther2/AtaskTracker/1.0.0"

    fun getTaskUrl() : String {
        return "$baseUrl/task"
    }

    fun getAuthUrl (): String {
        return "$baseUrl/auth"
    }
}