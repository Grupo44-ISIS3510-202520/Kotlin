package com.example.brigadeapp.presentation.ui.report

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.R

@Composable
internal fun PlayAudioButton(filePath: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var player: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (!isPlaying && filePath != null) {
                player = MediaPlayer().apply {
                    setDataSource(filePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        isPlaying = false
                        release()
                        player = null
                    }
                }
                isPlaying = true
                Toast.makeText(context, R.string.Playing_audio, Toast.LENGTH_SHORT).show()
            } else {
                player?.stop()
                player?.release()
                player = null
                isPlaying = false
                Toast.makeText(context, R.string.Stoping_audio, Toast.LENGTH_SHORT).show()
            }
        },
        enabled = filePath != null,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth().height(53.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
            contentDescription = stringResource(R.string.ToPlayAudio),
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}