package org.hse.ataskmobileclient.services

import android.content.Context
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.viewmodels.TaskListItem
import org.hse.ataskmobileclient.viewmodels.TasksHeader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TasksGroupingUtil {

    companion object {
        fun getGroupedDeadlineTasks(context : Context, ungroupedDeadlineTasks : ArrayList<Task>) : ArrayList<TaskListItem> {

            val overdueTasks : ArrayList<TaskListItem> = arrayListOf()
            val todayTasks : ArrayList<TaskListItem> = arrayListOf()
            val tomorrowTasks : ArrayList<TaskListItem> = arrayListOf()
            val upcomingTasks : ArrayList<TaskListItem> = arrayListOf()

            val (dueToday, dueTomorrow) = getTodayAndTomorrow()

            for (task in ungroupedDeadlineTasks) {
                if (task.dueDate == null)
                    continue

                val compareToToday = DateTimeComparer.compareDateOnly(task.dueDate, dueToday)
                if (compareToToday < 0) {
                    overdueTasks.add(task)
                    continue
                }
                else if (compareToToday == 0) {
                    todayTasks.add(task)
                    continue
                }

                val compareToTomorrow = DateTimeComparer.compareDateOnly(task.dueDate, dueTomorrow)
                if (compareToTomorrow == 0)
                    tomorrowTasks.add(task)
                else
                    upcomingTasks.add(task)
            }


            val sdf = SimpleDateFormat("dd MMMM", Locale.getDefault())
            val overdueStringHeader = context.getString(R.string.overdue_tasks_header)
            val todayStringHeader = context.getString(R.string.today_tasks_header)
            val tomorrowStringHeader = context.getString(R.string.tomorrow_tasks_header)
            val upcomingStringHeader = context.getString(R.string.upcoming_tasks_header)

            val overdueHeader = TasksHeader(overdueStringHeader)
            val todayHeader = TasksHeader("${todayStringHeader}, ${sdf.format(dueToday)}")
            val tomorrowHeader = TasksHeader("${tomorrowStringHeader}, ${sdf.format(dueTomorrow)}")
            val upcomingHeader = TasksHeader(upcomingStringHeader)

            val groupedDeadlineTasks : ArrayList<TaskListItem> = arrayListOf()
            if (overdueTasks.any()) {
                groupedDeadlineTasks.add(overdueHeader)
                groupedDeadlineTasks.addAll(overdueTasks)
            }

            if (todayTasks.any()) {
                groupedDeadlineTasks.add(todayHeader)
                groupedDeadlineTasks.addAll(todayTasks)
            }

            if (tomorrowTasks.any()) {
                groupedDeadlineTasks.add(tomorrowHeader)
                groupedDeadlineTasks.addAll(tomorrowTasks)
            }

            if (upcomingTasks.any()) {
                groupedDeadlineTasks.add(upcomingHeader)
                groupedDeadlineTasks.addAll(upcomingTasks)
            }

            if (!groupedDeadlineTasks.any()) {
                groupedDeadlineTasks.add(TasksHeader("Задач нет"))
            }

            return groupedDeadlineTasks
        }

        fun getGroupedBacklogTasks(context : Context, ungroupedBacklogTasks : ArrayList<Task>) : ArrayList<TaskListItem> {
            val groupedBacklogTasks : ArrayList<TaskListItem> = arrayListOf()

            val labelGroups = ungroupedBacklogTasks.groupBy { it.label }

            for (group in labelGroups) {
                val headerString = group.key ?: ":( Нет лейбла"
                groupedBacklogTasks.add(TasksHeader(headerString))
                groupedBacklogTasks.addAll(group.value.filter { it.dueDate == null })
            }

            if (!groupedBacklogTasks.any())
                groupedBacklogTasks.add(TasksHeader("Задач нет"))

            return groupedBacklogTasks
        }

        private fun getTodayAndTomorrow() : Pair<Date, Date> {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val today = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val tomorrow = calendar.time

            return Pair(today, tomorrow)
        }
    }
}