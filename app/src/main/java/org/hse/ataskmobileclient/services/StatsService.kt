package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.StatsApi
import java.util.*

class StatsService : IStatsService {

    override suspend fun getCompletedDeadlineTasksStats(authToken : String, startTime: Date?, endTime: Date?): Float? {
        val statsDto = StatsApi(authToken).getCompletedDeadlineTasksStats(startTime, endTime)
            ?: return null

        return statsDto.done.toFloat() / statsDto.total
    }

    override suspend fun getCompletedBacklogTasksStats(authToken: String, label: String?): Float? {
        val statsDto = StatsApi(authToken).getCompletedBacklogTasksStats(label)
            ?: return null

        return statsDto.done.toFloat() / statsDto.total
    }
}