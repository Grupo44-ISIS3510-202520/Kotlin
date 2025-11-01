package com.example.brigadeapp.view.components

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.brigadeapp.R
import java.io.File

@Composable
internal fun RecordAudioButton(
    modifier: Modifier = Modifier,
    onAudioSaved: (File?) -> Unit,
    onPathSaved: (String) -> Unit
) {
    val context = LocalContext.current
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }

    var audioFile by rememberSaveable(stateSaver = Saver<File?, String>(
        save = { it?.absolutePath ?: "" },
        restore = { if (it.isNotEmpty()) File(it) else null }
    )) { mutableStateOf(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, R.string.Denied_microphone, Toast.LENGTH_SHORT).show()
        }
    }

    Button(
        onClick = {
            if (!isRecording) {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp3")
                    audioFile = file

                    recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MediaRecorder(context)
                    } else {
                        MediaRecorder()
                    }

                    recorder?.apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setOutputFile(file.absolutePath)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        prepare()
                        start()
                    }

                    isRecording = true
                    Toast.makeText(context, R.string.Recording, Toast.LENGTH_SHORT).show()
                }
            } else {
                recorder?.apply {
                    stop()
                    release()
                }
                recorder = null
                isRecording = false
                Toast.makeText(context, R.string.Record_saved, Toast.LENGTH_SHORT).show()
                audioFile?.let {
                    onAudioSaved(it)
                    onPathSaved(it.absolutePath)
                }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) Color(0xFF9C27B0) else Color(0xFF2962FF)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.width(120.dp)
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Filled.Mic else Icons.Outlined.MicOff,
            contentDescription = stringResource(R.string.Record_audio),
            tint = Color.White,
            modifier = Modifier.size(35.dp)
        )
    }
}
