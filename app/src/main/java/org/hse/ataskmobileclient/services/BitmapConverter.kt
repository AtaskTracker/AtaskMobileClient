package org.hse.ataskmobileclient.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream


class BitmapConverter {

    companion object {
        fun fromBase64(photoBase64: String) : Bitmap {
            val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.count())
        }

        fun toBase64(photo: Bitmap) : String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }
}