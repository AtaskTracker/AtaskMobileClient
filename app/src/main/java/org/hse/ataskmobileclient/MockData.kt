package org.hse.ataskmobileclient

import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskListItem
import org.hse.ataskmobileclient.models.TasksHeader

class MockData {

    companion object {
        val Tasks : ArrayList<TaskListItem> = arrayListOf(
                TasksHeader("Выгулять собак"),
                Task(false, "Выгулять первую собаку"),
                Task(true, "Выгулять вторую собаку"),
                Task(true, "Выгулять третью собаку")
        )
    }
}