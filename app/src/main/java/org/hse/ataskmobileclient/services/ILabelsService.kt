package org.hse.ataskmobileclient.services

interface ILabelsService {
    suspend fun getAvailableLabels() : List<String>
    suspend fun postNewLabel(label: String)
}