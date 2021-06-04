package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.TasksApi
import org.hse.ataskmobileclient.dto.LabelDto
import org.hse.ataskmobileclient.dto.TaskDto
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskMember
import org.hse.ataskmobileclient.utils.UuidUtils
import java.text.SimpleDateFormat
import java.util.*

class TasksService : ITasksService {
    private val taskDueDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    private val usersService : IUsersService = UsersService()

    override suspend fun getAllTasks(
        token: String,
        startTime: Date?,
        endTime: Date?,
        label: String?
    ): List<Task> {
        val taskResults = TasksApi().getAllTasks(token)
        var allTasks = taskResults.map { taskResult -> convertTaskDtoToTask(taskResult) }

        if (startTime != null)
            allTasks = allTasks.filter { it.dueDate != null && DateTimeComparer.compareDateOnly(it.dueDate, startTime) >= 0 }

        if (endTime != null)
            allTasks = allTasks.filter { it.dueDate != null && DateTimeComparer.compareDateOnly(it.dueDate, endTime) <= 0 }

        if (label != null)
            allTasks = allTasks.filter { it.label == label }

        return allTasks
    }

    override suspend fun deleteTask(token: String, task: Task): Boolean {
        val taskDto = convertTaskToTaskDto(task)
        TasksApi().deleteTask(token, taskDto)

        return true
    }

    override suspend fun updateTask(token: String, task: Task): Task? {
        val taskDto = convertTaskToTaskDto(task)
        val taskResult = TasksApi().updateTask(token, taskDto)
            ?: return null

        return convertTaskDtoToTask(taskResult)
    }

    override suspend fun addTask(token: String, task: Task): Task? {
        val taskDto = convertTaskToTaskDto(task)
        val updatedTaskDto = TasksApi().createTask(token, taskDto) ?: return null

        return convertTaskDtoToTask(updatedTaskDto)
    }

    private fun convertTaskDtoToTask(taskDto: TaskDto) : Task {
        val dueDate =
            if (taskDto.dueDate != null) taskDueDateFormat.parse(taskDto.dueDate)
            else null

        val members = taskDto.participants?.map { email ->
            TaskMember(
                UUID(0L, 0L),
                email,
                ""
            )
        }

        return Task(
            taskDto.uuid,
            taskDto.status == "done",
            taskDto.summary,
            taskDto.description,
            dueDate,
            members ?: listOf(),
            taskDto.labels?.firstOrNull()?.summary,
            taskDto.photo,
        )
    }

    private fun convertTaskToTaskDto (task: Task) : TaskDto {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val dueDate =
            if (task.dueDate == null) null
            else sdf.format(task.dueDate)
        val members = task.members.map { member -> member.email }.toCollection(ArrayList<String>())

        return TaskDto(
            task.id ?: "",
            task.taskName,
            task.description,
            task.taskPictureBase64,
            if (task.isCompleted) "done" else "not done",
            dueDate,
            members,
            arrayListOf(LabelDto(task.label ?: "", ""))
        )
    }
}