package edu.bluejack25_1.synwc.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.IOException

class CloudinaryManager(context: Context) {
    private val cloudinary: Cloudinary
    private val appContext: Context = context.applicationContext

    init {
        // CLOUDINARY_URL=cloudinary://<your_api_key>:<your_api_secret>@dembbszgm
        val config = mapOf(
            "cloud_name" to "dembbszgm",
            "api_key" to "938274573161919",
            "api_secret" to "HmhkdegGRNnZzrQb5JYHv4wHZsA"
        )
        cloudinary = Cloudinary(config)
        Log.d("CloudinaryManager", "Cloudinary initialized")
    }

    suspend fun uploadImage(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("CloudinaryManager", "Starting image upload for URI: $uri")

                // Convert URI to File
                val file = uriToFile(uri)
                Log.d("CloudinaryManager", "Converted URI to file: ${file.absolutePath}")

                val result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                Log.d("CloudinaryManager", "Cloudinary upload result: $result")

                val url = result["url"] as? String
                if (url != null) {
                    Log.d("CloudinaryManager", "Image uploaded successfully: $url")

                    // Clean up temporary file
                    if (file.exists()) {
                        file.delete()
                    }

                    Result.success(url)
                } else {
                    Log.e("CloudinaryManager", "Failed to get image URL from result")
                    Result.failure(IOException("Failed to get image URL from Cloudinary"))
                }
            } catch (e: Exception) {
                Log.e("CloudinaryManager", "Error uploading image: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        return try {
            val inputStream: InputStream? = appContext.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                throw IOException("Cannot open input stream for URI: $uri")
            }

            // Create a temporary file
            val file = File.createTempFile("upload_", ".jpg", appContext.cacheDir)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            throw IOException("Failed to convert URI to file: ${e.message}", e)
        }
    }

    // Alternative method using InputStream directly
    suspend fun uploadImageWithInputStream(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("CloudinaryManager", "Starting image upload with InputStream for URI: $uri")

                val inputStream = appContext.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e("CloudinaryManager", "Cannot open input stream for URI: $uri")
                    return@withContext Result.failure(IOException("Cannot open input stream"))
                }

                inputStream.use { stream ->
                    val result = cloudinary.uploader().upload(stream, ObjectUtils.emptyMap())
                    Log.d("CloudinaryManager", "Cloudinary upload result: $result")

                    val url = result["url"] as? String
                    if (url != null) {
                        Log.d("CloudinaryManager", "Image uploaded successfully: $url")
                        Result.success(url)
                    } else {
                        Log.e("CloudinaryManager", "Failed to get image URL from result")
                        Result.failure(IOException("Failed to get image URL from Cloudinary"))
                    }
                }
            } catch (e: Exception) {
                Log.e("CloudinaryManager", "Error uploading image with InputStream: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}