package com.example.brigadeapp.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.core.auth.AuthClient
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.viewmodel.utils.RcpViewModel
import com.example.brigadeapp.R

@Composable
fun RcpScreen(
    auth: AuthClient,
    onBack: () -> Unit = {},
    viewModel: RcpViewModel = hiltViewModel()
) {
    var isGuiding by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopGuidance()
        }
    }

    StandardScreen(title = stringResource(R.string.RCP)) { inner ->
        Box(modifier = Modifier.fillMaxSize().padding(10.dp),
            contentAlignment = Alignment.Center) {
            if (!isGuiding) {
                EmergencyButton(onClick = {
                    isGuiding = true
                    viewModel.startGuidance()
                })
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.RCP_Progress) + "\n" + stringResource(R.string.RCP_Keep), textAlign = TextAlign.Center)
                    Button(onClick = {
                        isGuiding = false
                        viewModel.stopGuidance()
                    }) {
                        Text(stringResource(R.string.STOP))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24842)),
        modifier = Modifier
            .size(220.dp)
            .padding(24.dp),
        shape = CircleShape
    ) {
        Text(
            text = stringResource(R.string.CPR),
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}