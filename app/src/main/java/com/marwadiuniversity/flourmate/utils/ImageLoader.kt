package com.marwadiuniversity.flourmate.utils

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.marwadiuniversity.flourmate.R
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ImageLoader {

    fun loadImage(
        imageView: ImageView,
        imageUriString: String?,
        placeholderResId: Int = R.drawable.ic_flour_grain
    ) {
        if (imageUriString.isNullOrEmpty()) {
            imageView.setImageResource(placeholderResId)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            return
        }

        try {
            // Check if it's an HTTP/HTTPS URL (e.g. Firebase Storage download URL)
            if (imageUriString.startsWith("http://") || imageUriString.startsWith("https://")) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val url = URL(imageUriString)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connectTimeout = 5000
                        conn.readTimeout = 5000
                        conn.doInput = true
                        conn.connect()
                        val input: InputStream = conn.inputStream
                        val bitmap = BitmapFactory.decodeStream(input)
                        input.close()
                        conn.disconnect()

                        withContext(Dispatchers.Main) {
                            if (bitmap != null) {
                                imageView.imageTintList = null
                                imageView.setPadding(0, 0, 0, 0)
                                imageView.setImageBitmap(bitmap)
                                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            } else {
                                imageView.setImageResource(placeholderResId)
                                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            imageView.setImageResource(placeholderResId)
                            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                    }
                }
                return
            }

            // Check direct file path (with or without file:// prefix)
            val filePath = if (imageUriString.startsWith("file://")) {
                Uri.parse(imageUriString).path
            } else if (!imageUriString.startsWith("content://")) {
                imageUriString
            } else {
                null
            }

            if (!filePath.isNullOrEmpty()) {
                val file = File(filePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        imageView.imageTintList = null
                        imageView.setPadding(0, 0, 0, 0)
                        imageView.setImageBitmap(bitmap)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        return
                    }
                }
            }

            // Try Uri content resolver stream
            val uri = Uri.parse(imageUriString)
            val context = imageView.context
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (bitmap != null) {
                    imageView.imageTintList = null
                    imageView.setPadding(0, 0, 0, 0)
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    return
                }
            }

            // Fallback to placeholder if image loading fails
            imageView.setImageResource(placeholderResId)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        } catch (e: Exception) {
            e.printStackTrace()
            imageView.setImageResource(placeholderResId)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }
}