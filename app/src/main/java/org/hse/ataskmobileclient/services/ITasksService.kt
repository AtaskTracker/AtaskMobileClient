package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.dto.Task

interface ITasksService {

    suspend fun getAllTasksAsync() : List<Task>
}