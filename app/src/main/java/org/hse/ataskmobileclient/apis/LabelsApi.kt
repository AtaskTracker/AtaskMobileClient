package org.hse.ataskmobileclient.apis

import android.util.Log
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.hse.ataskmobileclient.dto.LabelDto
import org.hse.ataskmobileclient.models.Urls

class LabelsApi {
    suspend fun getAvailableLabels(token: String) : List<LabelDto> {

        val (_, response, result) = Urls().getLabelUrl()
            .httpGet()
            .header("Authorization", "Bearer $token")
            .awaitStringResponseResult()

        val labelsListType = object : TypeToken<ArrayList<LabelDto>>() {}.type
        return result.fold(
            { success ->
                Gson().fromJson(success, labelsListType) as ArrayList<LabelDto>
            },
            { error ->
                Log.e(TAG, "Ошибка при запросе доступных лейблов, ${response.statusCode} ${error.message}")
                listOf()
            }
        )
    }

    companion object {
        private const val TAG = "LabelsApi"
    }
}