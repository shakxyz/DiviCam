package com.example.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class GalleryRepository(private val context: Context) {

    suspend fun saveBitmapToGallery(
        bitmap: Bitmap,
        quality: Int,
        customName: String? = null,
        format: String = "WebP"
    ): Uri? = withContext(Dispatchers.IO) {
        val isWebP = format.equals("webp", ignoreCase = true)
        val ext = if (isWebP) "webp" else "jpg"
        val mime = if (isWebP) "image/webp" else "image/jpeg"
        val finalQuality = quality.coerceIn(50, 100)
        val filename = customName ?: "DiviCam_${System.currentTimeMillis()}.$ext"
        
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, mime)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DiviCam")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val contentResolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = contentResolver.insert(collection, values)

        if (uri != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { stream ->
                    val compressFormat = if (isWebP) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            Bitmap.CompressFormat.WEBP_LOSSY
                        } else {
                            @Suppress("DEPRECATION")
                            Bitmap.CompressFormat.WEBP
                        }
                    } else {
                        Bitmap.CompressFormat.JPEG
                    }
                    val success = bitmap.compress(compressFormat, finalQuality, stream)
                    if (!success) {
                        Log.e("GalleryRepository", "Failed to compress bitmap as $format")
                    }
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, values, null, null)
                }
                Log.d("GalleryRepository", "Successfully saved image to Gallery: $uri")
                return@withContext uri
            } catch (e: Exception) {
                Log.e("GalleryRepository", "Error saving bitmap to MediaStore", e)
                try {
                    contentResolver.delete(uri, null, null)
                } catch (ex: Exception) {
                    // ignore
                }
            }
        }
        return@withContext null
    }

    suspend fun saveVideoToGallery(tempFile: File): Uri? = withContext(Dispatchers.IO) {
        val filename = "DiviCamVideo_${System.currentTimeMillis()}.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/DiviCam")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val contentResolver = context.contentResolver
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val uri = contentResolver.insert(collection, values)

        if (uri != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    tempFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    contentResolver.update(uri, values, null, null)
                }
                Log.d("GalleryRepository", "Successfully saved video to Gallery: $uri")
                
                // Clean up temp file
                if (tempFile.exists()) {
                    tempFile.delete()
                }
                
                return@withContext uri
            } catch (e: Exception) {
                Log.e("GalleryRepository", "Error saving video to MediaStore", e)
                try {
                    contentResolver.delete(uri, null, null)
                } catch (ex: Exception) {
                    // ignore
                }
            }
        }
        return@withContext null
    }
}
