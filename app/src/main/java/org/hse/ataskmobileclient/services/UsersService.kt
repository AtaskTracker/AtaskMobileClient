package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.apis.UsersApi
import org.hse.ataskmobileclient.models.User
import org.hse.ataskmobileclient.utils.UuidUtils
import java.util.*

class UsersService : IUsersService {
    override suspend fun getUserByEmail(token: String, email: String): User? {
        val userDto = UsersApi().getUserByEmail(token, email)
            ?: return null

        val userId = UUID.fromString(UuidUtils.toUuidWithDashes(userDto.uuid!!))
        return User(
            userId,
            userDto.email,
            userDto.pictureUrl
        )
    }
}