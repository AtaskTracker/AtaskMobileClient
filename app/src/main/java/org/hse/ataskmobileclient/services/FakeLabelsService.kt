package org.hse.ataskmobileclient.services

class FakeLabelsService : ILabelsService {
    override suspend fun getAvailableLabelsAsync(): List<String> {
        return listOf(
            "Критичные",
            "Важные",
            "Неважные"
        )
    }
}