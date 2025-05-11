
package com.example.chat.api

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File

object ApiUtils {
    fun createMultipart(file: File): MultipartBody.Part {
        val mediaType = "application/octet-stream".toMediaTypeOrNull()
        val requestBody = file.asRequestBody(mediaType)
        return MultipartBody.Part.createFormData("file", file.name, requestBody)
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(uri, context.contentResolver) ?: "temp_upload"
            val tempFile = File(context.cacheDir, fileName)

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("UriToFile", "Failed to convert URI to file: ${e.localizedMessage}")
            null
        }
    }

    fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
        val returnCursor = contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            return it.getString(nameIndex)
        }
        return null
    }
}
