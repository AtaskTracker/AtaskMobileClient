package org.hse.ataskmobileclient.utils

import android.content.Context
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.services.DateTimeComparer
import org.hse.ataskmobileclient.viewmodels.TaskListItem
import org.hse.ataskmobileclient.viewmodels.TasksHeader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TasksGroupingUtil {

    companion object {
        fun getGroupedDeadlineTasks(context : Context, ungroupedDeadlineTasks : List<Task>) : ArrayList<TaskListItem> {

            val overdueTasks : ArrayList<TaskListItem> = arrayListOf()
            val todayTasks : ArrayList<TaskListItem> = arrayListOf()
            val tomorrowTasks : ArrayList<TaskListItem> = arrayListOf()
            val upcomingTasks : ArrayList<TaskListItem> = arrayListOf()
            val completedTasks: ArrayList<TaskListItem> = arrayListOf()

            val (dueToday, dueTomorrow) = getTodayAndTomorrow()

            for (task in ungroupedDeadlineTasks) {
                if (task.dueDate == null)
                    continue

                val compareToToday = DateTimeComparer.compareDateOnly(task.dueDate, dueToday)
                if (compareToToday < 0) {
                    if (task.isCompleted)
                        completedTasks.add(task)
                    else
                        overdueTasks.add(task)
                    continue
                }

                if (compareToToday == 0) {
                    todayTasks.add(task)
                    continue
                }

                val compareToTomorrow = DateTimeComparer.compareDateOnly(task.dueDate, dueTomorrow)
                if (compareToTomorrow == 0)
                    tomorrowTasks.add(task)
                else
                    upcomingTasks.add(task)
            }

            val locale = context.resources.configuration.locales.get(0)
            val sdf = SimpleDateFormat("dd MMMM", locale)
            val overdueStringHeaderString = context.getString(R.string.overdue_tasks_header)
            val todayStringHeaderString = context.getString(R.string.today_tasks_header)
            val tomorrowStringHeaderString = context.getString(R.string.tomorrow_tasks_header)
            val upcomingStringHeaderString = context.getString(R.string.upcoming_tasks_header)
            val completedDeadlineTasksHeaderString = context.getString(R.string.deadline_completed_tasks_header)

            val overdueHeader = TasksHeader(overdueStringHeaderString)
            val todayHeader = TasksHeader("${todayStringHeaderString}, ${sdf.format(dueToday)}")
            val tomorrowHeader = TasksHeader("${tomorrowStringHeaderString}, ${sdf.format(dueTomorrow)}")
            val upcomingHeader = TasksHeader(upcomingStringHeaderString)
            val completedTasksHeader = TasksHeader(completedDeadlineTasksHeaderString)

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

            if (completedTasks.any()) {
                groupedDeadlineTasks.add(completedTasksHeader)
                groupedDeadlineTasks.addAll(completedTasks)
            }

            if (!groupedDeadlineTasks.any()) {
                val noTasksHeaderString = context.getString(R.string.task_header_no_tasks)
                groupedDeadlineTasks.add(TasksHeader(noTasksHeaderString))
            }

            return groupedDeadlineTasks
        }

        fun getGroupedBacklogTasks(context : Context, ungroupedBacklogTasks : List<Task>) : ArrayList<TaskListItem> {
            val groupedBacklogTasks : ArrayList<TaskListItem> = arrayListOf()

            val labelGroups = ungroupedBacklogTasks.groupBy { it.label }
            val unlabeledTasks : ArrayList<Task> = arrayListOf()

            for (group in labelGroups) {
                if (group.key == null) {
                    unlabeledTasks.addAll(group.value.filter { it.dueDate == null})
                    continue
                }

                val headerString = group.key!!
                groupedBacklogTasks.add(TasksHeader(headerString))
                groupedBacklogTasks.addAll(group.value.filter { it.dueDate == null })
            }

            groupedBacklogTasks.add(TasksHeader(context.getString(R.string.tasks_header_no_label)))
            groupedBacklogTasks.addAll(unlabeledTasks)

            if (!groupedBacklogTasks.any()) {
                val noTasksHeaderString = context.getString(R.string.task_header_no_tasks)
                groupedBacklogTasks.add(TasksHeader(noTasksHeaderString))
            }

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