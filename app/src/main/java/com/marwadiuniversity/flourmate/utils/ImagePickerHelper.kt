package com.marwadiuniversity.flourmate.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog

object ImagePickerHelper {

    const val REQUEST_CODE_PICK_GALLERY = 2001
    const val REQUEST_CODE_CAPTURE_CAMERA = 2002

    fun showImagePickerDialog(
        activity: Activity,
        onRemoveSelected: (() -> Unit)? = null
    ) {
        val options = if (onRemoveSelected != null) {
            arrayOf("Choose from Gallery 🖼️", "Take Photo with Camera 📸", "Remove Image ❌")
        } else {
            arrayOf("Choose from Gallery 🖼️", "Take Photo with Camera 📸")
        }

        AlertDialog.Builder(activity)
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGalleryPicker(activity)
                    1 -> openCameraCapture(activity)
                    2 -> onRemoveSelected?.invoke()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun openGalleryPicker(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_GALLERY)
    }

    fun openCameraCapture(activity: Activity) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMERA)
        }
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        activity: Activity,
        prefix: String,
        onImageProcessed: (String) -> Unit
    ) {
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_CODE_PICK_GALLERY -> {
                val selectedUri: Uri? = data?.data
                if (selectedUri != null) {
                    val savedPath = ImageStorageHelper.saveImagePermanently(activity, selectedUri, prefix)
                    onImageProcessed(savedPath)
                }
            }
            REQUEST_CODE_CAPTURE_CAMERA -> {
                val photoBitmap = data?.extras?.get("data") as? Bitmap
                if (photoBitmap != null) {
                    val savedPath = ImageStorageHelper.saveBitmapPermanently(activity, photoBitmap, prefix)
                    onImageProcessed(savedPath)
                } else {
                    val selectedUri: Uri? = data?.data
                    if (selectedUri != null) {
                        val savedPath = ImageStorageHelper.saveImagePermanently(activity, selectedUri, prefix)
                        onImageProcessed(savedPath)
                    }
                }
            }
        }
    }
}