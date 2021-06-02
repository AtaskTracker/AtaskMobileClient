package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.hse.ataskmobileclient.models.TaskResult
import org.hse.ataskmobileclient.models.Urls

class API {

    init{
        FuelManager.instance.baseHeaders = mapOf("User-Agent" to "SwiftQube Wikipedia")
    }

    fun getAllTasks(responseHandler : (result: ArrayList<TaskResult>) -> Unit?) {
        Urls().getTaskUrl().httpGet()
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        Log.i("ErrorMsg", result.getException().message)
                        result.getException().stackTrace
                        throw Exception(result.getException())
                    }
                    is Result.Success -> {
                        val data = result.get()
                        val listType = object : TypeToken<ArrayList<TaskResult?>?>() {}.type
                        val taskResult: ArrayList<TaskResult> = Gson().fromJson(data, listType)
                        responseHandler.invoke(taskResult)
                    }
                }
            }
    }
}
