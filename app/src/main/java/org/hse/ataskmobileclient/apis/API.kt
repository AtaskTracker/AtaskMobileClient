package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.hse.ataskmobileclient.models.AuthResult
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskResult
import org.hse.ataskmobileclient.models.Urls

class API {

    // Headers can be changed here
//    init{
//        FuelManager.instance.baseHeaders = mapOf("User-Agent" to "AClient")
//    }

    fun auth (token: String, responseHandler: (result: AuthResult) -> Unit?) {
        Urls()
            .getAuthUrl()
            .httpPost(listOf("code" to token))
            .responseString { _, _, result ->
                this.authResultHandler(result, responseHandler)
            }
    }

    fun createTask(
        task: TaskResult,
        responseHandler : (result: ArrayList<TaskResult>) -> Unit?
    ) {
        Urls()
            .getTaskUrl()
            .httpPost()
            .jsonBody(task) // haven't checked if it is working
            .responseString { _, _, result ->
                this.tasksResultHandler(result, responseHandler)
            }
    }

    fun getAllTasks(responseHandler : (result: ArrayList<TaskResult>) -> Unit?) {
        Urls().getTaskUrl().httpGet()
            .responseString { _, _, result ->
                this.tasksResultHandler(result, responseHandler)
            }
    }


//    private fun createTaskResultHandler (
//        result : Result<String, FuelError>,
//        responseHandler: (result: TaskResult) -> Unit?
//    ) {
//        when (result) {
//            is Result.Failure -> {
//                Log.i("ErrorMsg", result.getException().message)
//                result.getException().stackTrace
//                throw Exception(result.getException())
//            }
//            is Result.Success -> {
//                val resultType = object : TypeToken<TaskResult?>() {}.type
//                val taskResult: TaskResult? = Gson().fromJson(
//                    result.get(), resultType
//                )
//                if (taskResult != null) {
//                    responseHandler.invoke(taskResult)
//                }
//            }
//        }
//    }

    private fun authResultHandler (
        result : Result<String, FuelError>,
        responseHandler: (result: AuthResult) -> Unit?
    ) {
        when (result) {
            is Result.Failure -> {
                Log.i("ErrorMsg", result.getException().message)
                result.getException().stackTrace
                throw Exception(result.getException())
            }
            is Result.Success -> {
                val authResult: AuthResult? = Gson().fromJson(result.get(), AuthResult::class.java)
                if (authResult != null) {
                    responseHandler.invoke(authResult)
                }
            }
        }
    }

    private fun tasksResultHandler (
        result : Result<String, FuelError>,
        responseHandler: (result: ArrayList<TaskResult>) -> Unit?
    )  {
        when (result) {
            is Result.Failure -> {
                Log.i("ErrorMsg", result.getException().message)
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
}
