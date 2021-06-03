package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.TasksApi
import org.hse.ataskmobileclient.dto.LabelDto
import org.hse.ataskmobileclient.dto.TaskDto
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

    override suspend fun deleteTask(token: String, task: Task): Boolean {
        val taskDto = dtoTaskConverter(task)
        TasksApi().deleteTask(token, taskDto)

        return true
    }

    override suspend fun updateTask(token: String, task: Task): Boolean {
        val taskDto = dtoTaskConverter(task)
        val taskResult = TasksApi().updateTask(token, taskDto)

        return taskResult != null
    }

    override suspend fun addTask(token: String, task: Task): Boolean {
        val taskDto = dtoTaskConverter(task)
        val taskResult = TasksApi().createTask(token, taskDto)

        return taskResult != null
    }

    private fun dtoTaskConverter (task: Task) : TaskDto {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val dateVal = sdf.format(task.dueDate ?: Date())
        val members = task.members?.map { member -> member.email }.toCollection(ArrayList<String>())

        return TaskDto(
            task.id,
            task.taskName,
            task.description,
            task.taskPictureBase64,
            if (task.isCompleted) "done" else "not done",
            dateVal,
            members,
            arrayListOf(LabelDto(task.label ?: "", ""))
        )
    }
}