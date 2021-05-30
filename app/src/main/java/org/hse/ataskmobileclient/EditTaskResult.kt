package org.hse.ataskmobileclient

import org.hse.ataskmobileclient.dto.Task

class EditTaskResult(
    val statusCode: EditTaskStatusCode,
    val editedTask : Task
)

enum class EditTaskStatusCode {
    DELETE,
    UPDATE,
    ADD,
}