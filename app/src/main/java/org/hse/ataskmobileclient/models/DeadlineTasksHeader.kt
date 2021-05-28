package org.hse.ataskmobileclient.models

import java.util.*

class DeadlineTasksHeader(
    val tasksDueDate : Date,
    headerString : String) : TasksHeader(headerString)