package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.models.TasksCompletionStats
import java.util.*

interface IStatsService {

    suspend fun getTasksCompletionStats(authToken: String, startTime: Date?,
                                        endTime: Date?, label: String?) : TasksCompletionStats?
}