package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.hse.ataskmobileclient.dto.GoogleImageDto
import org.hse.ataskmobileclient.dto.TaskDto
import org.hse.ataskmobileclient.models.Urls
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TasksApi {

    private val sdf = SimpleDateFormat("yyyy-MM-dd")

    suspend fun createTask(
        token: String,
        taskDto: TaskDto,
    ) :TaskDto? {
        val (_, response, result) = Urls().getTaskUrl()
            .httpPost()
            .header("Authorization", "Bearer $token")
            .jsonBody(taskDto) // haven't checked if it is working
            .awaitStringResponseResult()

        val jsonBody = Gson().toJson(taskDto)
        Log.i(TAG, "Добавление задачи, тело запроса: $jsonBody")

        return result.fold(
            { data -> Gson().fromJson(data, TaskDto::class.java) },
            {
                Log.e(TAG, "Ошибка при создании задачи: ${it.exception.message}, ${response.statusCode}")
                return null
            }
        )
    }


    suspend fun deleteTask(
        token: String,
        taskDto: TaskDto,
    ) {
        val (_, response, result) = Urls().getTaskUrl(taskDto.uuid!!) // /task/{id}
            .httpDelete()
            .header("Authorization", "Bearer $token")
            .awaitStringResponseResult()

        return result.fold(
            { data -> Log.d(TAG, "Результат удаления: $data")},
            {
                Log.e(TAG, "Ошибка при удалении задачи: ${it.exception.message}, ${response.statusCode}")
            }
        )
    }

    suspend fun updateTask(
        token: String,
        taskDto: TaskDto,
    ) :TaskDto? {
        if (taskDto.uuid == null)
            return null


        val url = Urls().getTaskUrl(taskDto.uuid)

        val (_, response, result) = Urls().getTaskUrl(taskDto.uuid) // /task/{id}
            .httpPut()
            .header("Authorization", "Bearer $token")
            .jsonBody(taskDto) // haven't checked if it is working
            .awaitStringResponseResult()
        Log.d(TAG, url)

        return result.fold(
            { data -> Gson().fromJson(data, TaskDto::class.java) },
            {
                Log.e(TAG, "Ошибка при обновлении задачи: ${it.exception.message}, ${response.statusCode}")
                return null
            }
        )
    }

    suspend fun getAllTasks(token: String, startTime: Date?, endTime: Date?, label: String?): List<TaskDto> {

        val parameters = arrayListOf<Pair<String, Any?>>()
        if (startTime != null)
            parameters.add("dateFrom" to sdf.format(startTime))

        if (endTime != null)
            parameters.add("dateTo" to sdf.format(endTime))

        if (label != null)
            parameters.add("label" to sdf.format(label))

        val listType = object : TypeToken<ArrayList<TaskDto>?>() {}.type
        val (request, response, result) = Urls().getTaskUrl().httpGet(parameters)
            .header("Authorization", "Bearer $token")
            .awaitStringResponseResult()

        Log.i(TAG, "getAllTasks url=${request.url}")

        return result.fold(
            { data -> Gson().fromJson(data, listType) as ArrayList<TaskDto> },
            {
                Log.e(TAG, "Ошибка при запросе задач: ${it.exception.message}, ${response.statusCode}")
                return listOf()
            }
        )
    }

    private fun tasksResultHandler (
        result : Result<String, FuelError>,
        responseHandler: (result: ArrayList<TaskDto>) -> Unit?
    )  {
        when (result) {
            is Result.Failure -> {
                Log.i("ErrorMsg", result.getException().message ?: "")
                result.getException().stackTrace
                throw Exception(result.getException())
            }
            is Result.Success -> {
                val listType = object : TypeToken<ArrayList<TaskDto?>?>() {}.type
                val taskResult: ArrayList<TaskDto> = Gson().fromJson(result.get(), listType)
                responseHandler.invoke(taskResult)
            }
        }
    }

    companion object {
        private const val TAG = "TasksApi"
    }
}
