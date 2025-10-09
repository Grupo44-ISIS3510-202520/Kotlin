package com.example.brigadeapp.presentation.ui.report

import android.Manifest
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.brigadeapp.R

private fun makeCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = "tel:$number".toUri()
    }
    context.startActivity(intent)
}

@Composable
internal fun CallButton(
    number: String,
    onCall: () -> Unit,
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
            onCall()
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24842)),
        modifier = modifier.size(90.dp)
    ) {
        Icon(Icons.Filled.Phone, contentDescription = stringResource(R.string.Emergency_Phone), tint = Color.White, modifier = Modifier.size(50.dp))
    }
}