package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.models.Task
import java.util.*

interface ITasksService {

    suspend fun getAllTasks(token: String, startTime : Date?, endTime : Date?, label : String?) : List<Task>

    suspend fun deleteTask(token: String, task: Task) : Boolean
    
    suspend fun updateTask(token: String, task: Task) : Boolean

    suspend fun addTask(token: String, task: Task) : Boolean
}