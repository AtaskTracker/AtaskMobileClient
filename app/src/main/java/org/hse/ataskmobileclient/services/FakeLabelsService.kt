package org.hse.ataskmobileclient.services

class FakeLabelsService : ILabelsService {
    override suspend fun getAvailableLabels(token: String): List<String> {
        return listOf(
            "Критичные",
            "Важные",
            "Неважные",
        )
    }
}