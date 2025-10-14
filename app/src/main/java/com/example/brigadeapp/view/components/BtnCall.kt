package com.example.brigadeapp.view.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.R

private fun makeCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$number")
    }
    context.startActivity(intent)
}

@Composable
internal fun CallButton(
    number: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            makeCall(context, number)
        } else {
            Toast.makeText(context, R.string.Denied_call, Toast.LENGTH_SHORT).show()
        }
    }

    Button(
        onClick = {
            launcher.launch(Manifest.permission.CALL_PHONE)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24842)),
        shape = CircleShape,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 24.dp)
            .size(230.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                clip = false
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = "Emergency",
                tint = Color.White,
                modifier = Modifier.size(90.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text("EMERGENCY", color = Color.White)
        }
    }
}