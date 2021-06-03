package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.TasksApi
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskMember
import java.text.SimpleDateFormat
import java.util.*

class TasksService : ITasksService {
    override suspend fun getAllTasks(
        token: String,
        startTime: Date?,
        endTime: Date?,
        label: String?
    ): List<Task> {
        val taskResults = TasksApi().getAllTasks(token)

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        return taskResults.map { taskResult ->

            val dueDate =
                if (taskResult.date != null) sdf.parse(taskResult.date)
                else null

            val members = taskResult.members?.map { email ->
                TaskMember(
                    UUID(0L, 0L),
                    email,
                    ""
                )
            }

            Task(
                taskResult.id ?: UUID(0L, 0L),
                taskResult.status == "done",
                taskResult.summary,
                taskResult.description,
                dueDate,
                members ?: listOf()
            )
        }
    }

    override suspend fun deleteTask(task: Task): Boolean {
        return true
    }

    override suspend fun updateTask(task: Task): Boolean {
        return true
    }

    override suspend fun addTask(task: Task): Boolean {
        return true
    }
}