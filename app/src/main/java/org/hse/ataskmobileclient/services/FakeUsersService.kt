package org.hse.ataskmobileclient.services

import kotlinx.coroutines.delay
import org.hse.ataskmobileclient.models.User
import java.util.*

class FakeUsersService : IUsersService {
    override suspend fun getUserByEmail(email: String): User? {
        val allUsers = getAllUsers()
        delay(500)
        return allUsers.firstOrNull { it.email == email }
    }

    private fun getAllUsers() : List<User> {
        return listOf(
            User(UUID.randomUUID(), "test@google.com", "https://i.natgeofe.com/n/4f5aaece-3300-41a4-b2a8-ed2708a0a27c/domestic-dog_thumb_square.jpg?w=136&h=136"),
            User(UUID.randomUUID(), "example@example.com", "https://www.dogstrust.org.uk/help-advice/_images/164742v800_puppy-1.jpg"),
            User(UUID.randomUUID(), "edkartashov@edu.hse.ru", "https://i.guim.co.uk/img/media/7a633730f5f90db3c12f6efc954a2d5b475c3d4a/0_138_5544_3327/master/5544.jpg?width=1200&height=1200&quality=85&auto=format&fit=crop&s=27c09d27ccbd139fd0f7d1cef8f7d41d"),
        )
    }
}