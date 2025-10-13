package com.example.brigadeapp.presentation.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.presentation.viewmodel.ReportViewModel
import com.example.brigadeapp.presentation.viewmodel.UploadFileViewModel
import java.io.File

@Composable
internal fun CameraButton(
    modifier: Modifier = Modifier,
    onPhotoSaved: (File?) -> Unit
) {
    val context = LocalContext.current
    var photoFile by rememberSaveable(stateSaver = Saver<File?, String>(
        save = { it?.absolutePath ?: "" },
        restore = { if (it.isNotEmpty()) File(it) else null }
    )) { mutableStateOf(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, R.string.Denied_camera, Toast.LENGTH_SHORT).show()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            Toast.makeText(context, R.string.Photo_saved, Toast.LENGTH_SHORT).show()
            onPhotoSaved(photoFile)
        } else {
            photoFile = null
        }
    }

    Button(
        onClick = {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                photoFile = file
                cameraLauncher.launch(uri)
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.width(120.dp)
    ) {
        Icon(
            Icons.Filled.CameraAlt,
            contentDescription = stringResource(R.string.Take_photo),
            tint = Color.White,
            modifier = Modifier.size(35.dp)
        )
    }
}