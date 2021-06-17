package org.hse.ataskmobileclient.models

import kotlin.math.roundToInt

class TasksCompletionStats(
    val backlogTasksCompletedCount : Int,
    val backlogTasksTotalCount: Int,
    val deadlineTasksCompletedCount: Int,
    val deadlineTasksTotalCount: Int,
) {

    val backlogCompletionPercentage: Int
        get() = (backlogTasksCompletedCount.toFloat() / backlogTasksTotalCount * 100).roundToInt()

    val deadlineCompletionPercentage: Int
        get() =
            (deadlineTasksCompletedCount.toFloat() / deadlineTasksTotalCount * 100).roundToInt()
}