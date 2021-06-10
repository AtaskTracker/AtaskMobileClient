package org.hse.ataskmobileclient.models

import org.hse.ataskmobileclient.viewmodels.TaskListItem
import java.util.*

class Task(
        val id: String?,
        val isCompleted : Boolean,
        val taskName : String,
        val description : String,
        val dueDate: Date?,
        val members: List<TaskMember>,
        val label: String? = null,
        var photoUrl: String? = null,
        val photoBase64 : String? = null,
) : TaskListItem()