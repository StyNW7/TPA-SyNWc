package edu.bluejack25_1.synwc.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class ImagePicker {
    @Composable
    fun rememberImagePicker(
        onImageSelected: (Uri?) -> Unit
    ): () -> Unit {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            onImageSelected(uri)
        }

        return remember {
            {
                launcher.launch("image/*")
            }
        }
    }
}