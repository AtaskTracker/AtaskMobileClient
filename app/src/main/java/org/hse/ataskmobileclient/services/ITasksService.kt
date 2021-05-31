package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.models.Task

interface ITasksService {

    suspend fun getAllTasksAsync() : List<Task>
}