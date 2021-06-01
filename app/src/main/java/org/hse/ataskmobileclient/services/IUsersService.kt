package org.hse.ataskmobileclient.services

import org.hse.ataskmobileclient.models.User

interface IUsersService {
    suspend fun getUserByEmail(email: String) : User?
}