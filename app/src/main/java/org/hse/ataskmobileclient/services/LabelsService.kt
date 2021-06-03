package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.LabelsApi

class LabelsService : ILabelsService {
    override suspend fun getAvailableLabels(token: String): List<String> {
        val availableLabels = LabelsApi().getAvailableLabels(token)
        return availableLabels.map { it.summary }
    }
}