package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.StatsApi
import org.hse.ataskmobileclient.models.TasksCompletionStats
import java.util.*

class StatsService : IStatsService {

    override suspend fun getTasksCompletionStats(
        authToken: String,
        startTime: Date?,
        endTime: Date?,
        label: String?
    ): TasksCompletionStats? {
        val deadlineStatsDto = StatsApi(authToken).getCompletedDeadlineTasksStats(startTime, endTime)
            ?: return null

        val backlogStatsDto = StatsApi(authToken).getCompletedBacklogTasksStats(label)
            ?: return null

        return TasksCompletionStats(
            backlogStatsDto.done,
            backlogStatsDto.total,
            deadlineStatsDto.done,
            deadlineStatsDto.total,
        )
    }
}