package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.hse.ataskmobileclient.dto.AuthDto
import org.hse.ataskmobileclient.models.AuthResult
import org.hse.ataskmobileclient.models.TaskResult
import org.hse.ataskmobileclient.models.Urls

class TasksApi {

    fun createTask(
        token: String,
        task: TaskResult,
        responseHandler : (result: ArrayList<TaskResult>) -> Unit?
    ) {
        val url = Urls().getTaskUrl()
        Urls()
            .getTaskUrl()
            .httpPost()
            .header("Authorization", "Bearer $token")
            .jsonBody(task) // haven't checked if it is working
            .responseString { _, _, result ->
                this.tasksResultHandler(result, responseHandler)
            }
    }

    suspend fun getAllTasks(token: String): List<TaskResult> {
        val listType = object : TypeToken<ArrayList<TaskResult?>?>() {}.type
        val (_, response, result) = Urls().getTaskUrl().httpGet()
            .header("Authorization", "Bearer $token")
            .awaitStringResponseResult()

        return result.fold(
            { data -> Gson().fromJson(data, listType) as List<TaskResult> },
            {
                Log.e(TAG, "Ошибка при запросе задач: ${it.exception.message}, ${response.statusCode}")
                return listOf()
            }
        )
    }

    private fun tasksResultHandler (
        result : Result<String, FuelError>,
        responseHandler: (result: ArrayList<TaskResult>) -> Unit?
    )  {
        when (result) {
            is Result.Failure -> {
                Log.i("ErrorMsg", result.getException().message ?: "")
                result.getException().stackTrace
                throw Exception(result.getException())
            }
            is Result.Success -> {
                val listType = object : TypeToken<ArrayList<TaskResult?>?>() {}.type
                val taskResult: ArrayList<TaskResult> = Gson().fromJson(result.get(), listType)
                responseHandler.invoke(taskResult)
            }
        }
    }

    companion object {
        private const val TAG = "TasksApi"
    }
}
