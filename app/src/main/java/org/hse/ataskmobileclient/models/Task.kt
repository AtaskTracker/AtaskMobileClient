package org.hse.ataskmobileclient.models

import java.util.*

class Task(
        val id: UUID,
        val isCompleted : Boolean,
        val taskName : String,
        val description : String,
        val dueDate: Date?,
        val members: List<TaskMember>,
        val label: String? = null,
        val taskPictureBase64: String? = null,
) : TaskListItem()