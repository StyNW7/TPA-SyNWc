package edu.bluejack25_1.synwc.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

class ImagePicker {
    @Composable
    fun rememberImagePicker(onImagePicked: (Uri?) -> Unit): () -> Unit {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            onImagePicked(uri)
        }

        return {
            launcher.launch("image/*")
        }
    }
}