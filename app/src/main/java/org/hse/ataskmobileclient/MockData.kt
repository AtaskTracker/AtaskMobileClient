package org.hse.ataskmobileclient

import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskListItem
import org.hse.ataskmobileclient.models.TasksHeader

class MockData {

    companion object {
        val DeadlineTasks : ArrayList<TaskListItem> = arrayListOf(
                TasksHeader("Сегодня, 14 мая 2021"),
                Task(false, "Выгулять первую собаку"),
                Task(true, "Выгулять вторую собаку"),
                Task(true, "Выгулять третью собаку")
        )

        val BacklogTasks : ArrayList<TaskListItem> = arrayListOf(
            TasksHeader("Критичные"),
            Task(false, "Купить новые джинсы"),
            Task(true, "Сделать ремонт"),
            Task(true, "Отдать ненужные вещи")
        )
    }
}