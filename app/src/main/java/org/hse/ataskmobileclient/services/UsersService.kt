package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.UsersApi
import org.hse.ataskmobileclient.models.User
import java.util.*

class UsersService : IUsersService {
    override suspend fun getUserByEmail(token: String, email: String): User? {
        val userDto = UsersApi().getUserByEmail(token, email)
            ?: return null

        val userId = UUID.fromString(toUuidWithDashes(userDto.uuid!!))
        return User(
            userId,
            userDto.email,
            userDto.pictureUrl
        )
    }

    private fun toUuidWithDashes(uuid: String): String {
        var sb = StringBuilder(uuid)
        sb.insert(8, "-")
        sb = StringBuilder(sb.toString())
        sb.insert(13, "-")
        sb = StringBuilder(sb.toString())
        sb.insert(18, "-")
        sb = StringBuilder(sb.toString())
        sb.insert(23, "-")
        return sb.toString()
    }
}