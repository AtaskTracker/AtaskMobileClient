package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.models.Task
import java.util.*

interface ITasksService {

    suspend fun getAllTasksAsync(startTime : Date?, endTime : Date?, label : String?) : List<Task>
}