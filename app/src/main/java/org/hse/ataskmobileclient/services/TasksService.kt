package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.TasksApi
import org.hse.ataskmobileclient.dto.LabelDto
import org.hse.ataskmobileclient.dto.TaskDto
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskMember
import java.text.SimpleDateFormat
import java.util.*

class TasksService : ITasksService {
    private val taskDueDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    private val minDateValue = taskDueDateFormat.parse("0001-01-01T00:00:00Z")
    private val usersService : IUsersService = UsersService()

    override suspend fun getAllTasks(
        token: String,
        startTime: Date?,
        endTime: Date?,
        label: String?
    ): List<Task> {
        val taskResults = TasksApi().getAllTasks(token, startTime, endTime, label)

        return taskResults.map { taskResult -> convertTaskDtoToTask(taskResult) }
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
        val dueDate = parseDueDate(taskDto.dueDate)

        val members = taskDto.participants
            ?.filter { it.isNotEmpty() }
            ?.map { email ->
            TaskMember(
                email,
                ""
            )
        }

        val label = getTaskLabel(taskDto)

        return Task(
            taskDto.uuid,
            taskDto.status == "done",
            taskDto.summary,
            taskDto.description,
            dueDate,
            members ?: listOf(),
            label,
            taskDto.photo,
        )
    }

    private fun convertTaskToTaskDto (task: Task) : TaskDto {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val dueDate =
            if (task.dueDate == null) null
            else sdf.format(task.dueDate)
        val members = task.members.map { member -> member.email }.toCollection(ArrayList<String>())

        val labels =
            if (task.label == null) arrayListOf<LabelDto>()
            else arrayListOf(LabelDto(task.label, ""))

        return TaskDto(
            task.id ?: "",
            task.taskName,
            task.description,
            task.photoUrl,
            if (task.isCompleted) "done" else "not done",
            dueDate,
            members,
            labels
        )
    }

    private fun parseDueDate(dueDateStr: String?) : Date? {
        return if (dueDateStr == null) {
            null
        }
        else {
            val parsedValue = taskDueDateFormat.parse(dueDateStr)
            if (parsedValue != null && parsedValue > minDateValue)
                parsedValue
            else
                null
        }
    }

    private fun getTaskLabel(taskDto: TaskDto) : String? {
        val firstLabel = taskDto.labels?.firstOrNull()?.summary
        if (firstLabel.isNullOrEmpty())
            return null

        return firstLabel
    }
}