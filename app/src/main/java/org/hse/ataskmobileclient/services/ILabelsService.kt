package org.hse.ataskmobileclient.services

interface ILabelsService {
    suspend fun getAvailableLabels(token: String) : List<String>
}