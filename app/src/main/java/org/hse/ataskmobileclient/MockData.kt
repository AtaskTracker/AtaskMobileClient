package org.hse.ataskmobileclient

import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskListItem
import org.hse.ataskmobileclient.models.TaskMember
import org.hse.ataskmobileclient.models.TasksHeader
import java.util.*
import kotlin.collections.ArrayList

class MockData {
    companion object {

        val DeadlineTasks : ArrayList<TaskListItem> = generateDeadlineTasks()
        val BacklogTasks : ArrayList<TaskListItem> = generateBacklogTasks()

        private fun generateDeadlineTasks() : ArrayList<TaskListItem> {
            val calendar = Calendar.getInstance()
            val today = calendar.time
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val tomorrow = calendar.time

            return arrayListOf(
                TasksHeader("Сегодня, 14 мая 2021"),
                Task(
                    UUID.fromString("72b05ace-bda0-46e6-bb7b-5f7d213e7c79"),
                    false,
                    "Выгулять собаку",
                    "Нужно выгулять собаку",
                    today,
                    arrayListOf()),
                Task(
                    UUID.fromString("123adfba-5b14-4b22-956e-15417298f0b5"),
                    false,
                    "Выгулять кошку",
                    "Нужно выгулять кошку",
                    today,
                    arrayListOf()),
                Task(
                    UUID.fromString("195db63b-4586-4a59-9a4c-d8d1180fb719"),
                    false,
                    "Сходить в магазин",
                    "Нужно сходить в магазин",
                    today,
                    arrayListOf()),
                TasksHeader("Завтра, 15 мая 2021"),
                Task(
                    UUID.fromString("493fb710-a4ce-4415-8eba-3a5851009df7"),
                    false,
                    "Снова выгулять собаку",
                    "Нужно выгулять собаку, но уже завтра",
                    tomorrow,
                    arrayListOf()),
                Task(
                    UUID.fromString("2773ced7-edc2-4779-b148-4a6373559d05"),
                    false,
                    "Снова выгулять кошку",
                    "Нужно выгулять кошку, но уже завтра",
                    tomorrow,
                    arrayListOf()),
            )
        }

        private fun generateBacklogTasks() : ArrayList<TaskListItem> {
            return arrayListOf(
                TasksHeader("Критичные"),
                Task(
                    UUID.fromString("e67bc517-2933-43c2-b710-ed8913687a7e"),
                    true,
                    "Купить новые джинсы",
                    "",
                    null,
                    arrayListOf()),
                Task(
                    UUID.fromString("d7612102-f836-494f-92de-eef43032555b"),
                    false,
                    "Сделать ремонт",
                    "",
                    null,
                    arrayListOf()),
                Task(
                    UUID.fromString("2aaee91a-e40a-493b-a307-1601367afc20"),
                    false,
                    "Отдать ненужные вещи",
                    "",
                    null,
                    arrayListOf()),
                TasksHeader("Неважные"),
                Task(
                    UUID.fromString("75849030-970f-4c33-9f1a-ace34d9300e4"),
                    false,
                    "Посмотреть в окно",
                    "",
                    null,
                    arrayListOf()),
                Task(
                    UUID.fromString("bc536639-634b-4070-8e3f-363ec2d8324f"),
                    false,
                    "Поспать",
                    "",
                    null,
                    arrayListOf()),
            )
        }

    }
}