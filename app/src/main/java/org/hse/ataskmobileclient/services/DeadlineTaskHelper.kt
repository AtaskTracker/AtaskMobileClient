package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskListItem

// Временный сервис, который будет удален в будущем. Помогает группировать задачи по дедлайну
class DeadlineTaskHelper {

    fun getTasksGroupedByDeadline(tasks : ArrayList<Task>) : ArrayList<TaskListItem> {
        return arrayListOf()
    }
}