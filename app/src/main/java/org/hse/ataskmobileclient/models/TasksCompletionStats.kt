package org.hse.ataskmobileclient.models

import kotlin.math.roundToInt

class TasksCompletionStats(
    val backlogTasksCompletedCount : Int,
    val backlogTasksTotalCount: Int,
    val deadlineTasksCompletedCount: Int,
    val deadlineTasksTotalCount: Int,
) {

    val backlogCompletionPercentage: Int
        get() {
            if (backlogTasksTotalCount == 0)
                return 0

            return (backlogTasksCompletedCount.toFloat() / backlogTasksTotalCount * 100).roundToInt()
        }

    val deadlineCompletionPercentage: Int
        get() {
            if (deadlineTasksTotalCount == 0)
                return 0

            return (deadlineTasksCompletedCount.toFloat() / deadlineTasksTotalCount * 100).roundToInt()
        }
}