package org.hse.ataskmobileclient.services

interface ILabelsService {
    suspend fun getAvailableLabelsAsync() : List<String>
}