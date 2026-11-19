package com.marwadiuniversity.flourmate.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

    fun saveImagePermanently(context: Context, sourceUri: Uri, prefix: String): String {
        return try {
            val directory = File(context.filesDir, "flourmate_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
            val destFile = File(directory, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val orientedBitmap = rotateImageIfRequired(context, sourceUri, bitmap)
                val resizedBitmap = scaleBitmapIfNeeded(orientedBitmap, 1024)

                val outStream = FileOutputStream(destFile)
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                outStream.flush()
                outStream.close()

                Uri.fromFile(destFile).toString()
            } else {
                sourceUri.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }

    fun saveBitmapPermanently(context: Context, bitmap: Bitmap, prefix: String): String {
        return try {
            val directory = File(context.filesDir, "flourmate_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
            val destFile = File(directory, fileName)

            val resizedBitmap = scaleBitmapIfNeeded(bitmap, 1024)

            val outStream = FileOutputStream(destFile)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
            outStream.flush()
            outStream.close()

            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun deleteImageFile(imageUriString: String) {
        try {
            if (imageUriString.startsWith("file://")) {
                val path = Uri.parse(imageUriString).path
                if (path != null) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun rotateImageIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            inputStream.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}