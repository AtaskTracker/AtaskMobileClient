package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.models.Task

interface ITasksService {
    suspend fun getTasksForWeekAsync() : List<Task>
    suspend fun getBacklogTasksAsync() : List<Task>

    suspend fun updateTaskAsync()
}