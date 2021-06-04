package org.hse.ataskmobileclient.utils

class UuidUtils {

    companion object {

        fun toUuidWithDashes(uuid: String): String {
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

        fun toUuidWithoutDashes(uuid: String) : String = uuid.filter { it != '-' }
    }
}