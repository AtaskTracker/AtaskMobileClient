package org.hse.ataskmobileclient.services

class FakeLabelsService : ILabelsService {
    override suspend fun getAvailableLabels(): List<String> {
        return listOf(
            "Критичные",
            "Важные",
            "Неважные",
        )
    }

    override suspend fun postNewLabel(label: String) {

    }
}