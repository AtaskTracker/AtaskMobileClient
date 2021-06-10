package org.hse.ataskmobileclient.services

import java.util.*

interface IStatsService {

    suspend fun getCompletedDeadlineTasksStats(authToken : String, startTime: Date?, endTime: Date?) : Float?

    suspend fun getCompletedBacklogTasksStats(authToken : String, label: String?): Float?
}