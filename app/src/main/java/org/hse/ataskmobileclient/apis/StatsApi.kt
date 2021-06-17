package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.google.gson.Gson
import org.hse.ataskmobileclient.dto.StatsDto
import org.hse.ataskmobileclient.models.Urls
import java.text.SimpleDateFormat
import java.util.*

class StatsApi(
    private val authToken: String
    ) {

    private val gson = Gson()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getCompletedDeadlineTasksStats(startTime: Date?, endTime: Date?) : StatsDto? {
        val parameters = arrayListOf<Pair<String, Any?>>()

        if (startTime != null)
            parameters.add("dateFrom" to sdf.format(startTime))
        else
            parameters.add("dateFrom" to "0001-01-02")

        if (endTime != null)
            parameters.add("dateTo" to sdf.format(endTime))

        return requestStatsWithParameters(parameters)
    }

    suspend fun getCompletedBacklogTasksStats(label: String?): StatsDto? {
        val parameters = arrayListOf<Pair<String, Any?>>()

        parameters.add("dateFrom" to "0001-01-01")
        parameters.add("dateTo" to "0001-01-02")

        if (label != null)
            parameters.add("label" to label)

        return requestStatsWithParameters(parameters)
    }

    private suspend fun requestStatsWithParameters(parameters: List<Pair<String, Any?>>) : StatsDto? {
        val url = Urls().getStatsUrl()

        val (request, _, result) = Fuel
            .get(url, parameters)
            .header("Authorization" to "Bearer $authToken")
            .awaitStringResponseResult()

        Log.i(TAG, gson.toJson(parameters))
        Log.i(TAG, request.url.toString())

        return result.fold(
            success = { data ->
                val statsDto = gson.fromJson(data, StatsDto::class.java)
                statsDto
            },
            failure = { error ->
                Log.e(TAG, "Error while getting percentage of completed deadline tasks: " +
                        "${error.message}")
                null
            }
        )
    }

    companion object {
        private const val TAG = "StatsApi"
    }
}